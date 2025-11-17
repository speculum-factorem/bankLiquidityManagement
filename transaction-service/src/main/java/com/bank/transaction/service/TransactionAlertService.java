package com.bank.transaction.service;

import com.bank.transaction.model.TransactionAlert;
import com.bank.transaction.repository.TransactionAlertRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class TransactionAlertService {

    private final TransactionAlertRepository alertRepository;
    private final TransactionMetricsService metricsService;

    @Transactional(readOnly = true)
    public List<TransactionAlert> getAllAlerts() {
        log.debug("Fetching all transaction alerts");
        return alertRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<TransactionAlert> getActiveAlerts() {
        log.debug("Fetching active transaction alerts");
        return alertRepository.findByStatus(TransactionAlert.AlertStatus.ACTIVE);
    }

    @Transactional(readOnly = true)
    public List<TransactionAlert> getAlertsByAccount(String accountNumber) {
        log.debug("Fetching alerts for account: {}", accountNumber);
        return alertRepository.findByAccountNumber(accountNumber);
    }

    @Transactional(readOnly = true)
    public List<TransactionAlert> getHighSeverityAlerts(Integer minSeverity) {
        log.debug("Fetching high severity alerts (severity >= {})", minSeverity);
        return alertRepository.findBySeverityGreaterThanEqual(minSeverity);
    }

    @Transactional(readOnly = true)
    public List<TransactionAlert> getRecentHighSeverityAlerts() {
        log.debug("Fetching recent high severity alerts");
        LocalDateTime since = LocalDateTime.now().minusHours(24);
        return alertRepository.findRecentHighSeverityAlerts(since, 7);
    }

    public TransactionAlert reviewAlert(Long alertId, String reviewedBy, String notes) {
        log.info("Reviewing alert: {} by user: {}", alertId, reviewedBy);

        return alertRepository.findById(alertId)
                .map(alert -> {
                    alert.setStatus(TransactionAlert.AlertStatus.UNDER_REVIEW);
                    alert.setReviewedAt(LocalDateTime.now());
                    alert.setReviewedBy(reviewedBy);
                    alert.setInvestigationNotes(notes);

                    TransactionAlert updatedAlert = alertRepository.save(alert);
                    metricsService.recordAlertReview(updatedAlert);

                    log.info("Alert {} reviewed by {}", alertId, reviewedBy);
                    return updatedAlert;
                })
                .orElseThrow(() -> {
                    log.warn("Alert not found for review: {}", alertId);
                    return new RuntimeException("Alert not found with id: " + alertId);
                });
    }

    public TransactionAlert resolveAlert(Long alertId, String resolvedBy, String resolutionNotes) {
        log.info("Resolving alert: {}", alertId);

        return alertRepository.findById(alertId)
                .map(alert -> {
                    alert.setStatus(TransactionAlert.AlertStatus.RESOLVED);
                    alert.setResolvedAt(LocalDateTime.now());
                    alert.setResolvedBy(resolvedBy);
                    alert.setResolutionNotes(resolutionNotes);

                    TransactionAlert updatedAlert = alertRepository.save(alert);
                    metricsService.recordAlertResolution(updatedAlert);

                    log.info("Alert {} resolved by {}", alertId, resolvedBy);
                    return updatedAlert;
                })
                .orElseThrow(() -> {
                    log.warn("Alert not found for resolution: {}", alertId);
                    return new RuntimeException("Alert not found with id: " + alertId);
                });
    }

    public TransactionAlert markAsFalsePositive(Long alertId, String markedBy, String notes) {
        log.info("Marking alert as false positive: {}", alertId);

        return alertRepository.findById(alertId)
                .map(alert -> {
                    alert.setStatus(TransactionAlert.AlertStatus.FALSE_POSITIVE);
                    alert.setResolvedAt(LocalDateTime.now());
                    alert.setResolvedBy(markedBy);
                    alert.setResolutionNotes("False positive: " + notes);

                    TransactionAlert updatedAlert = alertRepository.save(alert);
                    metricsService.recordFalsePositiveAlert(updatedAlert);

                    log.info("Alert {} marked as false positive by {}", alertId, markedBy);
                    return updatedAlert;
                })
                .orElseThrow(() -> {
                    log.warn("Alert not found for false positive marking: {}", alertId);
                    return new RuntimeException("Alert not found with id: " + alertId);
                });
    }

    public Long getActiveAlertCount() {
        return alertRepository.countByStatus(TransactionAlert.AlertStatus.ACTIVE);
    }

    public Map<String, Long> getAlertStatistics() {
        LocalDateTime since = LocalDateTime.now().minusDays(7);
        List<Object[]> alertCounts = alertRepository.getAlertCountByTypeSince(since);

        return alertCounts.stream()
                .collect(Collectors.toMap(
                        obj -> ((TransactionAlert.AlertType) obj[0]).name(),
                        obj -> (Long) obj[1]
                ));
    }

    public Map<String, Long> getAlertStatusDistribution() {
        Map<String, Long> distribution = new java.util.HashMap<>();

        for (TransactionAlert.AlertStatus status : TransactionAlert.AlertStatus.values()) {
            Long count = alertRepository.countByStatus(status);
            distribution.put(status.name(), count);
        }

        return distribution;
    }
}