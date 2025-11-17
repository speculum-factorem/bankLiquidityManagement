package com.bank.transaction.health;

import com.bank.transaction.repository.TransactionRepository;
import com.bank.transaction.repository.TransactionAlertRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TransactionHealthIndicator implements HealthIndicator {

    private final TransactionRepository transactionRepository;
    private final TransactionAlertRepository alertRepository;

    @Override
    public Health health() {
        try {
            long totalTransactions = transactionRepository.count();
            long failedTransactions = transactionRepository.findByStatus(Transaction.TransactionStatus.FAILED).size();
            long activeAlerts = alertRepository.countByStatus(TransactionAlert.AlertStatus.ACTIVE);

            Health.Builder status = totalTransactions > 0 ? Health.up() : Health.unknown();

            return status
                    .withDetail("totalTransactions", totalTransactions)
                    .withDetail("failedTransactions", failedTransactions)
                    .withDetail("activeAlerts", activeAlerts)
                    .withDetail("successRate", calculateSuccessRate(totalTransactions, failedTransactions))
                    .build();

        } catch (Exception e) {
            log.error("Transaction health check failed", e);
            return Health.down()
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }

    private double calculateSuccessRate(long total, long failed) {
        if (total == 0) return 100.0;
        return ((double) (total - failed) / total) * 100;
    }
}