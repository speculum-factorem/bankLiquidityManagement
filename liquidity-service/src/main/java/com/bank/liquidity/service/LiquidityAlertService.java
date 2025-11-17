package com.bank.liquidity.service;

import com.bank.liquidity.model.LiquidityAlert;
import com.bank.liquidity.repository.LiquidityAlertRepository;
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
public class LiquidityAlertService {

    private final LiquidityAlertRepository alertRepository;
    private final LiquidityMetricsService metricsService;

    @Transactional(readOnly = true)
    public List<LiquidityAlert> getAllAlerts() {
        log.debug("Fetching all liquidity alerts");
        return alertRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<LiquidityAlert> getActiveAlerts() {
        log.debug("Fetching active liquidity alerts");
        return alertRepository.findByStatus(LiquidityAlert.AlertStatus.ACTIVE);
    }

    @Transactional(readOnly = true)
    public List<LiquidityAlert> getAlertsByBranch(String branchCode) {
        log.debug("Fetching alerts for branch: {}", branchCode);
        return alertRepository.findByBranchCode(branchCode);
    }

    @Transactional(readOnly = true)
    public List<LiquidityAlert> getHighSeverityAlerts(Integer minSeverity) {
        log.debug("Fetching high severity alerts (severity >= {})", minSeverity);
        return alertRepository.findBySeverityGreaterThanEqual(minSeverity);
    }

    public LiquidityAlert acknowledgeAlert(Long alertId, String acknowledgedBy) {
        log.info("Acknowledging alert: {} by user: {}", alertId, acknowledgedBy);

        return alertRepository.findById(alertId)
                .map(alert -> {
                    alert.setStatus(LiquidityAlert.AlertStatus.ACKNOWLEDGED);
                    alert.setAcknowledgedAt(LocalDateTime.now());
                    alert.setAcknowledgedBy(acknowledgedBy);

                    LiquidityAlert updatedAlert = alertRepository.save(alert);
                    metricsService.recordAlertAcknowledgment(updatedAlert);

                    log.info("Alert {} acknowledged by {}", alertId, acknowledgedBy);
                    return updatedAlert;
                })
                .orElseThrow(() -> {
                    log.warn("Alert not found for acknowledgment: {}", alertId);
                    return new RuntimeException("Alert not found with id: " + alertId);
                });
    }

    public LiquidityAlert resolveAlert(Long alertId, String resolutionNotes) {
        log.info("Resolving alert: {}", alertId);

        return alertRepository.findById(alertId)
                .map(alert -> {
                    alert.setStatus(LiquidityAlert.AlertStatus.RESOLVED);
                    alert.setResolvedAt(LocalDateTime.now());
                    alert.setResolutionNotes(resolutionNotes);

                    LiquidityAlert updatedAlert = alertRepository.save(alert);
                    metricsService.recordAlertResolution(updatedAlert);

                    log.info("Alert {} resolved", alertId);
                    return updatedAlert;
                })
                .orElseThrow(() -> {
                    log.warn("Alert not found for resolution: {}", alertId);
                    return new RuntimeException("Alert not found with id: " + alertId);
                });
    }

    public Long getActiveAlertCount() {
        return alertRepository.countByStatus(LiquidityAlert.AlertStatus.ACTIVE);
    }
}