package com.bank.risk.service;

import com.bank.risk.model.RiskAlert;
import com.bank.risk.repository.RiskAlertRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class RiskAlertService {

    private final RiskAlertRepository alertRepository;
    private final RiskMetricsService metricsService;

    @Transactional(readOnly = true)
    public List<RiskAlert> getAllAlerts() {
        log.debug("Fetching all risk alerts");
        return alertRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<RiskAlert> getActiveAlerts() {
        log.debug("Fetching active risk alerts");
        return alertRepository.findByStatus(RiskAlert.AlertStatus.ACTIVE);
    }

    @Transactional(readOnly = true)
    public List<RiskAlert> getAlertsByBranch(String branchCode) {
        log.debug("Fetching alerts for branch: {}", branchCode);
        return alertRepository.findByBranchCode(branchCode);
    }

    @Transactional(readOnly = true)
    public List<RiskAlert> getHighSeverityAlerts(Integer minSeverity) {
        log.debug("Fetching high severity alerts (severity >= {})", minSeverity);
        return alertRepository.findBySeverityGreaterThanEqual(minSeverity);
    }

    @Transactional(readOnly = true)
    public List<RiskAlert> getRecentCriticalAlerts() {
        log.debug("Fetching recent critical alerts");
        LocalDateTime since = LocalDateTime.now().minusHours(24);
        return alertRepository.findByRiskLevelAndCreatedAtAfter("CRITICAL", since);
    }

    public RiskAlert acknowledgeAlert(Long alertId, String acknowledgedBy) {
        log.info("Acknowledging risk alert: {} by user: {}", alertId, acknowledgedBy);

        return alertRepository.findById(alertId)
                .map(alert -> {
                    alert.setStatus(RiskAlert.AlertStatus.ACKNOWLEDGED);
                    alert.setAcknowledgedAt(LocalDateTime.now());
                    alert.setAcknowledgedBy(acknowledgedBy);

                    RiskAlert updatedAlert = alertRepository.save(alert);
                    metricsService.recordAlertAcknowledgment(updatedAlert);

                    log.info("Risk alert {} acknowledged by {}", alertId, acknowledgedBy);
                    return updatedAlert;
                })
                .orElseThrow(() -> {
                    log.warn("Risk alert not found for acknowledgment: {}", alertId);
                    return new RuntimeException("Risk alert not found with id: " + alertId);
                });
    }

    public RiskAlert resolveAlert(Long alertId, String resolutionNotes) {
        log.info("Resolving risk alert: {}", alertId);

        return alertRepository.findById(alertId)
                .map(alert -> {
                    alert.setStatus(RiskAlert.AlertStatus.RESOLVED);
                    alert.setResolvedAt(LocalDateTime.now());
                    alert.setResolutionNotes(resolutionNotes);

                    RiskAlert updatedAlert = alertRepository.save(alert);
                    metricsService.recordAlertResolution(updatedAlert);

                    log.info("Risk alert {} resolved", alertId);
                    return updatedAlert;
                })
                .orElseThrow(() -> {
                    log.warn("Risk alert not found for resolution: {}", alertId);
                    return new RuntimeException("Risk alert not found with id: " + alertId);
                });
    }

    public RiskAlert markAsFalsePositive(Long alertId, String notes) {
        log.info("Marking risk alert as false positive: {}", alertId);

        return alertRepository.findById(alertId)
                .map(alert -> {
                    alert.setStatus(RiskAlert.AlertStatus.FALSE_POSITIVE);
                    alert.setResolvedAt(LocalDateTime.now());
                    alert.setResolutionNotes("False positive: " + notes);

                    RiskAlert updatedAlert = alertRepository.save(alert);
                    metricsService.recordFalsePositiveAlert(updatedAlert);

                    log.info("Risk alert {} marked as false positive", alertId);
                    return updatedAlert;
                })
                .orElseThrow(() -> {
                    log.warn("Risk alert not found for false positive marking: {}", alertId);
                    return new RuntimeException("Risk alert not found with id: " + alertId);
                });
    }

    public Long getActiveAlertCount() {
        return alertRepository.countByStatus(RiskAlert.AlertStatus.ACTIVE);
    }

    public Map<String, Long> getAlertStatistics() {
        LocalDateTime since = LocalDateTime.now().minusDays(7);
        List<Object[]> alertCounts = alertRepository.getAlertCountByTypeSince(since);

        return alertCounts.stream()
                .collect(Collectors.toMap(
                        obj -> ((RiskAlert.AlertType) obj[0]).name(),
                        obj -> (Long) obj[1]
                ));
    }
}