package com.bank.transaction.service;

import com.bank.transaction.dto.TransactionRequest;
import com.bank.transaction.dto.TransactionResponse;
import com.bank.transaction.dto.TransactionStatusUpdate;
import com.bank.transaction.model.Transaction;
import com.bank.transaction.model.TransactionAlert;
import com.bank.transaction.repository.TransactionAlertRepository;
import com.bank.transaction.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final TransactionAlertRepository alertRepository;
    private final TransactionMetricsService metricsService;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public TransactionResponse createTransaction(TransactionRequest request) {
        log.info("Creating transaction for account: {}, type: {}, amount: {}",
                request.getAccountNumber(), request.getType(), request.getAmount());

        String correlationId = MDC.get("correlationId");

        // Создание объекта транзакции из запроса
        Transaction transaction = Transaction.builder()
                .type(request.getType())
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .accountNumber(request.getAccountNumber())
                .counterpartyAccountNumber(request.getCounterpartyAccountNumber())
                .description(request.getDescription())
                .branchCode(request.getBranchCode())
                .channel(request.getChannel())
                .referenceNumber(request.getReferenceNumber())
                .category(request.getCategory())
                .build();

        // Сохранение транзакции в базе данных (транзакция БД)
        Transaction savedTransaction = transactionRepository.save(transaction);

        try {
            // Публикация в Kafka для других сервисов (асинхронно, неблокирующе)
            publishTransactionToKafka(savedTransaction);

            // Проверка на алерты (асинхронно)
            checkForTransactionAlerts(savedTransaction);

            // Запись метрик
            metricsService.recordTransactionCreation(savedTransaction);

            log.info("Transaction created successfully: {} for account: {}",
                    savedTransaction.getTransactionId(), savedTransaction.getAccountNumber());

            return mapToResponse(savedTransaction);
        } catch (Exception e) {
            // Если публикация в Kafka не удалась, транзакция уже сохранена
            // Это приемлемо, так как у нас есть обработка DLQ
            log.error("Error in post-transaction processing for transaction: {}", 
                savedTransaction.getTransactionId(), e);
            // Все равно возвращаем успех, так как транзакция сохранена
            return mapToResponse(savedTransaction);
        }
    }

    @Transactional(readOnly = true)
    public List<TransactionResponse> getAllTransactions() {
        log.debug("Fetching all transactions");
        return transactionRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Optional<TransactionResponse> getTransactionById(Long id) {
        log.debug("Fetching transaction by id: {}", id);
        return transactionRepository.findById(id)
                .map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public Optional<TransactionResponse> getTransactionByTransactionId(String transactionId) {
        log.debug("Fetching transaction by transactionId: {}", transactionId);
        return transactionRepository.findByTransactionId(transactionId)
                .map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public List<TransactionResponse> getTransactionsByAccount(String accountNumber) {
        log.debug("Fetching transactions for account: {}", accountNumber);
        return transactionRepository.findByAccountNumber(accountNumber).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TransactionResponse> getRecentTransactionsByAccount(String accountNumber, int limit) {
        log.debug("Fetching recent {} transactions for account: {}", limit, accountNumber);
        return transactionRepository.findRecentTransactionsByAccount(accountNumber, limit).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TransactionResponse> getTransactionsByStatus(Transaction.TransactionStatus status) {
        log.debug("Fetching transactions with status: {}", status);
        return transactionRepository.findByStatus(status).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TransactionResponse> getHighValueTransactions(BigDecimal minAmount) {
        log.debug("Fetching high value transactions with min amount: {}", minAmount);
        return transactionRepository.findByAmountGreaterThanEqual(minAmount).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public TransactionResponse updateTransactionStatus(Long id, TransactionStatusUpdate statusUpdate) {
        log.info("Updating transaction status for id: {} to {}", id, statusUpdate.getStatus());

        return transactionRepository.findById(id)
                .map(transaction -> {
                    Transaction.TransactionStatus oldStatus = transaction.getStatus();
                    transaction.setStatus(statusUpdate.getStatus());

                    if (statusUpdate.getFailureReason() != null) {
                        transaction.setFailureReason(statusUpdate.getFailureReason());
                    }

                    Transaction updatedTransaction = transactionRepository.save(transaction);

                    // Запись метрик изменения статуса
                    metricsService.recordTransactionStatusChange(updatedTransaction, oldStatus);

                    // Публикация обновления статуса в Kafka
                    publishTransactionStatusUpdate(updatedTransaction, oldStatus);

                    log.info("Transaction status updated: {} from {} to {}",
                            updatedTransaction.getTransactionId(), oldStatus, updatedTransaction.getStatus());

                    return mapToResponse(updatedTransaction);
                })
                .orElseThrow(() -> {
                    log.warn("Transaction not found for status update: {}", id);
                    return new RuntimeException("Transaction not found with id: " + id);
                });
    }

    public TransactionResponse updateTransactionStatusByTransactionId(String transactionId, TransactionStatusUpdate statusUpdate) {
        log.info("Updating transaction status for transactionId: {} to {}", transactionId, statusUpdate.getStatus());

        return transactionRepository.findByTransactionId(transactionId)
                .map(transaction -> {
                    Transaction.TransactionStatus oldStatus = transaction.getStatus();
                    transaction.setStatus(statusUpdate.getStatus());

                    if (statusUpdate.getFailureReason() != null) {
                        transaction.setFailureReason(statusUpdate.getFailureReason());
                    }

                    Transaction updatedTransaction = transactionRepository.save(transaction);

                    // Запись метрик изменения статуса
                    metricsService.recordTransactionStatusChange(updatedTransaction, oldStatus);

                    log.info("Transaction status updated: {} from {} to {}",
                            updatedTransaction.getTransactionId(), oldStatus, updatedTransaction.getStatus());

                    return mapToResponse(updatedTransaction);
                })
                .orElseThrow(() -> {
                    log.warn("Transaction not found for status update: {}", transactionId);
                    return new RuntimeException("Transaction not found with transactionId: " + transactionId);
                });
    }

    private void publishTransactionToKafka(Transaction transaction) {
        try {
            kafkaTemplate.send("transactions", transaction.getTransactionId(), transaction)
                .whenComplete((result, exception) -> {
                    if (exception != null) {
                        log.error("Failed to publish transaction to Kafka: {}", 
                            transaction.getTransactionId(), exception);
                        // Сохранение неудачного сообщения для повторной попытки или обработки DLQ
                        handleKafkaPublishFailure("transactions", transaction.getTransactionId(), 
                            transaction, exception);
                    } else {
                        log.debug("Transaction published to Kafka: {} to partition: {}, offset: {}", 
                            transaction.getTransactionId(), 
                            result != null ? result.getRecordMetadata().partition() : "unknown",
                            result != null ? result.getRecordMetadata().offset() : "unknown");
                    }
                });
        } catch (Exception e) {
            log.error("Failed to publish transaction to Kafka: {}", transaction.getTransactionId(), e);
            handleKafkaPublishFailure("transactions", transaction.getTransactionId(), transaction, e);
        }
    }

    private void publishTransactionStatusUpdate(Transaction transaction, Transaction.TransactionStatus oldStatus) {
        try {
            var statusUpdate = new TransactionStatusUpdateEvent(transaction, oldStatus);
            kafkaTemplate.send("transaction-status-updates", transaction.getTransactionId(), statusUpdate)
                .whenComplete((result, exception) -> {
                    if (exception != null) {
                        log.error("Failed to publish transaction status update to Kafka: {}", 
                            transaction.getTransactionId(), exception);
                        handleKafkaPublishFailure("transaction-status-updates", 
                            transaction.getTransactionId(), statusUpdate, exception);
                    } else {
                        log.debug("Transaction status update published to Kafka: {}", 
                            transaction.getTransactionId());
                    }
                });
        } catch (Exception e) {
            log.error("Failed to publish transaction status update to Kafka: {}", 
                transaction.getTransactionId(), e);
            var statusUpdate = new TransactionStatusUpdateEvent(transaction, oldStatus);
            handleKafkaPublishFailure("transaction-status-updates", 
                transaction.getTransactionId(), statusUpdate, e);
        }
    }

    // Обработка ошибки публикации в Kafka
    private void handleKafkaPublishFailure(String topic, String key, Object message, Throwable exception) {
        // Попытка отправить в DLQ топик
        try {
            String dlqTopic = topic + "-dlq";
            kafkaTemplate.send(dlqTopic, key, message);
            log.warn("Message sent to DLQ topic: {} for key: {}", dlqTopic, key);
        } catch (Exception dlqException) {
            log.error("Failed to send message to DLQ topic: {} for key: {}", 
                topic + "-dlq", key, dlqException);
            // В продакшн системе можно сохранить это в базу данных
            // или использовать паттерн outbox для гарантированной доставки
        }
    }

    // Проверка транзакции на наличие алертов
    private void checkForTransactionAlerts(Transaction transaction) {
        // Проверка на транзакции с высокой суммой
        if (transaction.isHighValueTransaction()) {
            createHighValueTransactionAlert(transaction);
        }

        // Проверка на подозрительные транзакции
        if (transaction.isSuspiciousTransaction()) {
            createSuspiciousTransactionAlert(transaction);
        }

        // Проверка на множественные неудачные транзакции
        checkForMultipleFailedTransactions(transaction.getAccountNumber());
    }

    private void createHighValueTransactionAlert(Transaction transaction) {
        TransactionAlert alert = TransactionAlert.builder()
                .alertType(TransactionAlert.AlertType.HIGH_VALUE_TRANSACTION)
                .status(TransactionAlert.AlertStatus.ACTIVE)
                .transactionId(transaction.getTransactionId())
                .accountNumber(transaction.getAccountNumber())
                .amount(transaction.getAmount())
                .currency(transaction.getCurrency())
                .message(String.format("High value transaction detected: %s %s for account %s",
                        transaction.getAmount(), transaction.getCurrency(), transaction.getAccountNumber()))
                .severity(6)
                .details("Transaction exceeds high value threshold")
                .build();

        alertRepository.save(alert);
        metricsService.recordTransactionAlert(alert);

        // Публикация алерта в Kafka
        try {
            kafkaTemplate.send("transaction-alerts", transaction.getTransactionId(), alert);
        } catch (Exception e) {
            log.error("Failed to publish transaction alert to Kafka", e);
        }

        log.warn("High value transaction alert created: {} for account: {}",
                transaction.getTransactionId(), transaction.getAccountNumber());
    }

    private void createSuspiciousTransactionAlert(Transaction transaction) {
        TransactionAlert alert = TransactionAlert.builder()
                .alertType(TransactionAlert.AlertType.SUSPICIOUS_ACTIVITY)
                .status(TransactionAlert.AlertStatus.ACTIVE)
                .transactionId(transaction.getTransactionId())
                .accountNumber(transaction.getAccountNumber())
                .amount(transaction.getAmount())
                .currency(transaction.getCurrency())
                .message(String.format("Suspicious transaction detected: %s %s for account %s",
                        transaction.getAmount(), transaction.getCurrency(), transaction.getAccountNumber()))
                .severity(9)
                .details("Transaction exceeds suspicious activity threshold")
                .build();

        alertRepository.save(alert);
        metricsService.recordTransactionAlert(alert);

        // Публикация подозрительного алерта в Kafka
        try {
            kafkaTemplate.send("transaction-alerts", transaction.getTransactionId(), alert);
            kafkaTemplate.send("high-value-transactions", transaction.getTransactionId(), transaction);
        } catch (Exception e) {
            log.error("Failed to publish suspicious transaction alert to Kafka", e);
        }

        log.error("SUSPICIOUS transaction alert created: {} for account: {}",
                transaction.getTransactionId(), transaction.getAccountNumber());
    }

    private void checkForMultipleFailedTransactions(String accountNumber) {
        LocalDateTime since = LocalDateTime.now().minusHours(1);
        Long failedCount = transactionRepository.countFailedTransactionsSince(accountNumber, since);

        if (failedCount >= 3) {
            createMultipleFailedTransactionsAlert(accountNumber, failedCount);
        }
    }

    private void createMultipleFailedTransactionsAlert(String accountNumber, Long failedCount) {
        TransactionAlert alert = TransactionAlert.builder()
                .alertType(TransactionAlert.AlertType.MULTIPLE_FAILED_ATTEMPTS)
                .status(TransactionAlert.AlertStatus.ACTIVE)
                .transactionId("SYSTEM-" + System.currentTimeMillis())
                .accountNumber(accountNumber)
                .amount(BigDecimal.ZERO)
                .currency("USD")
                .message(String.format("Multiple failed transactions detected for account %s: %d attempts",
                        accountNumber, failedCount))
                .severity(7)
                .details("Account shows multiple failed transaction attempts in a short period")
                .build();

        alertRepository.save(alert);
        metricsService.recordTransactionAlert(alert);

        log.warn("Multiple failed transactions alert created for account: {}, count: {}",
                accountNumber, failedCount);
    }

    private TransactionResponse mapToResponse(Transaction transaction) {
        return TransactionResponse.builder()
                .id(transaction.getId())
                .transactionId(transaction.getTransactionId())
                .type(transaction.getType())
                .amount(transaction.getAmount())
                .currency(transaction.getCurrency())
                .accountNumber(transaction.getAccountNumber())
                .counterpartyAccountNumber(transaction.getCounterpartyAccountNumber())
                .transactionDate(transaction.getTransactionDate())
                .status(transaction.getStatus())
                .description(transaction.getDescription())
                .failureReason(transaction.getFailureReason())
                .branchCode(transaction.getBranchCode())
                .channel(transaction.getChannel())
                .balanceAfter(transaction.getBalanceAfter())
                .balanceBefore(transaction.getBalanceBefore())
                .referenceNumber(transaction.getReferenceNumber())
                .category(transaction.getCategory())
                .createdAt(transaction.getCreatedAt())
                .updatedAt(transaction.getUpdatedAt())
                .highValueTransaction(transaction.isHighValueTransaction())
                .suspiciousTransaction(transaction.isSuspiciousTransaction())
                .build();
    }

    // Внутренний класс для событий Kafka
    @Data
    @Builder
    private static class TransactionStatusUpdateEvent {
        private String transactionId;
        private Transaction.TransactionStatus oldStatus;
        private Transaction.TransactionStatus newStatus;
        private LocalDateTime updateTime;
        private String accountNumber;
        private BigDecimal amount;
        private String currency;

        public TransactionStatusUpdateEvent(Transaction transaction, Transaction.TransactionStatus oldStatus) {
            this.transactionId = transaction.getTransactionId();
            this.oldStatus = oldStatus;
            this.newStatus = transaction.getStatus();
            this.updateTime = LocalDateTime.now();
            this.accountNumber = transaction.getAccountNumber();
            this.amount = transaction.getAmount();
            this.currency = transaction.getCurrency();
        }
    }
}