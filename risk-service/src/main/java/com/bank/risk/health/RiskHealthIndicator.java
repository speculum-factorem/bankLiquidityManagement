package com.bank.risk.health;

import com.bank.risk.repository.RiskAssessmentRepository;
import com.bank.risk.repository.RiskAlertRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RiskHealthIndicator implements HealthIndicator {

    private final RiskAssessmentRepository assessmentRepository;
    private final RiskAlertRepository alertRepository;

    @Override
    public Health health() {
        try {
            long totalAssessments = assessmentRepository.count();
            long criticalAssessments = assessmentRepository.countByRiskLevel(RiskAssessment.RiskLevel.CRITICAL);
            long activeAlerts = alertRepository.countByStatus(RiskAlert.AlertStatus.ACTIVE);

            Health.Builder status = totalAssessments > 0 ? Health.up() : Health.unknown();

            return status
                    .withDetail("totalAssessments", totalAssessments)
                    .withDetail("criticalAssessments", criticalAssessments)
                    .withDetail("activeAlerts", activeAlerts)
                    .withDetail("riskHealthPercentage", calculateHealthPercentage(totalAssessments, criticalAssessments))
                    .build();

        } catch (Exception e) {
            log.error("Risk health check failed", e);
            return Health.down()
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }

    private double calculateHealthPercentage(long total, long critical) {
        if (total == 0) return 100.0;
        return ((double) (total - critical) / total) * 100;
    }
}