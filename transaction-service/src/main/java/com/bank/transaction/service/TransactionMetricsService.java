package com.bank.transaction.service;

import com.bank.transaction.model.Transaction;
import com.bank.transaction.model.TransactionAlert;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class TransactionMetricsService {

    private final MeterRegistry meterRegistry;
    private final ConcurrentHashMap<String, Counter> counters = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Timer> timers = new ConcurrentHashMap<>();

    public void recordTransactionCreation(Transaction transaction) {
        incrementCounter("transactions.created",
                "type", transaction.getType().name(),
                "status", transaction.getStatus().name(),
                "currency", transaction.getCurrency());

        // Record transaction amount
        if (transaction.getAmount() != null) {
            meterRegistry.gauge("transactions.amount",
                    List.of(
                            meterRegistry.config().tag("type", transaction.getType().name()),
                            meterRegistry.config().tag("currency", transaction.getCurrency())
                    ),
                    transaction.getAmount(),
                    BigDecimal::doubleValue);
        }

        // Record high value transactions
        if (transaction.isHighValueTransaction()) {
            incrementCounter("transactions.high_value",
                    "type", transaction.getType().name(),
                    "currency", transaction.getCurrency());
        }

        // Record suspicious transactions
        if (transaction.isSuspiciousTransaction()) {
            incrementCounter("transactions.suspicious",
                    "type", transaction.getType().name(),
                    "currency", transaction.getCurrency());
        }

        log.debug("Recorded metrics for transaction: {} (type: {}, amount: {})",
                transaction.getTransactionId(), transaction.getType(), transaction.getAmount());
    }

    public void recordTransactionStatusChange(Transaction transaction, Transaction.TransactionStatus oldStatus) {
        incrementCounter("transactions.status.changes",
                "old_status", oldStatus.name(),
                "new_status", transaction.getStatus().name(),
                "type", transaction.getType().name());

        if (transaction.getStatus() == Transaction.TransactionStatus.FAILED) {
            incrementCounter("transactions.failed",
                    "type", transaction.getType().name(),
                    "reason", transaction.getFailureReason() != null ? transaction.getFailureReason() : "unknown");
        }

        log.debug("Recorded metrics for transaction status change: {} ({} -> {})",
                transaction.getTransactionId(), oldStatus, transaction.getStatus());
    }

    public void recordTransactionAlert(TransactionAlert alert) {
        incrementCounter("transaction.alerts.created",
                "type", alert.getAlertType().name(),
                "account", alert.getAccountNumber(),
                "severity", alert.getSeverity().toString());

        // Record alert severity gauge
        meterRegistry.gauge("transaction.alerts.severity",
                List.of(
                        meterRegistry.config().tag("type", alert.getAlertType().name()),
                        meterRegistry.config().tag("account", alert.getAccountNumber())
                ),
                alert.getSeverity(),
                Integer::doubleValue);

        log.debug("Recorded metrics for transaction alert: {} (type: {}, severity: {})",
                alert.getId(), alert.getAlertType(), alert.getSeverity());
    }

    public void recordAlertReview(TransactionAlert alert) {
        incrementCounter("transaction.alerts.reviewed",
                "type", alert.getAlertType().name(),
                "account", alert.getAccountNumber());

        log.debug("Recorded metrics for alert review: {}", alert.getId());
    }

    public void recordAlertResolution(TransactionAlert alert) {
        incrementCounter("transaction.alerts.resolved",
                "type", alert.getAlertType().name(),
                "account", alert.getAccountNumber());

        log.debug("Recorded metrics for alert resolution: {}", alert.getId());
    }

    public void recordFalsePositiveAlert(TransactionAlert alert) {
        incrementCounter("transaction.alerts.false_positive",
                "type", alert.getAlertType().name(),
                "account", alert.getAccountNumber());

        log.debug("Recorded metrics for false positive alert: {}", alert.getId());
    }

    public void recordTransactionProcessingTime(long duration, String transactionType, String status) {
        recordTimer("transactions.processing.duration", duration,
                "type", transactionType,
                "status", status);
    }

    private void incrementCounter(String name, String... tags) {
        String key = name + String.join("", tags);
        Counter counter = counters.computeIfAbsent(key,
                k -> Counter.builder(name)
                        .tags(tags)
                        .description("Transaction service metrics")
                        .register(meterRegistry));
        counter.increment();
    }

    private void recordTimer(String name, long duration, String... tags) {
        String key = name + String.join("", tags);
        Timer timer = timers.computeIfAbsent(key,
                k -> Timer.builder(name)
                        .tags(tags)
                        .description("Transaction service operation duration")
                        .register(meterRegistry));
        timer.record(duration, TimeUnit.MILLISECONDS);
    }
}