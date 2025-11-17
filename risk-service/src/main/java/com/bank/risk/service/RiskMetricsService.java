package com.bank.risk.service;

import com.bank.risk.model.RiskAlert;
import com.bank.risk.model.RiskAssessment;
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
public class RiskMetricsService {

    private final MeterRegistry meterRegistry;
    private final ConcurrentHashMap<String, Counter> counters = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Timer> timers = new ConcurrentHashMap<>();

    public void recordRiskAssessment(RiskAssessment assessment) {
        incrementCounter("risk.assessments.created",
                "branch", assessment.getBranchCode(),
                "currency", assessment.getCurrency(),
                "risk_level", assessment.getRiskLevel().name());

        // Record risk score gauge
        meterRegistry.gauge("risk.score",
                List.of(
                        meterRegistry.config().tag("branch", assessment.getBranchCode()),
                        meterRegistry.config().tag("currency", assessment.getCurrency())
                ),
                assessment.getRiskScore(),
                BigDecimal::doubleValue);

        // Record individual risk factors
        if (assessment.getLiquidityRisk() != null) {
            meterRegistry.gauge("risk.liquidity",
                    List.of(meterRegistry.config().tag("branch", assessment.getBranchCode())),
                    assessment.getLiquidityRisk(),
                    BigDecimal::doubleValue);
        }

        if (assessment.getConcentrationRisk() != null) {
            meterRegistry.gauge("risk.concentration",
                    List.of(meterRegistry.config().tag("branch", assessment.getBranchCode())),
                    assessment.getConcentrationRisk(),
                    BigDecimal::doubleValue);
        }

        log.debug("Recorded metrics for risk assessment: {} (branch: {}, score: {})",
                assessment.getId(), assessment.getBranchCode(), assessment.getRiskScore());
    }

    public void recordRiskAlert(RiskAlert alert) {
        incrementCounter("risk.alerts.created",
                "type", alert.getAlertType().name(),
                "branch", alert.getBranchCode(),
                "currency", alert.getCurrency(),
                "severity", alert.getSeverity().toString());

        // Record alert severity gauge
        meterRegistry.gauge("risk.alert.severity",
                List.of(
                        meterRegistry.config().tag("branch", alert.getBranchCode()),
                        meterRegistry.config().tag("type", alert.getAlertType().name())
                ),
                alert.getSeverity(),
                Integer::doubleValue);

        log.debug("Recorded metrics for risk alert: {} (type: {}, severity: {})",
                alert.getId(), alert.getAlertType(), alert.getSeverity());
    }

    public void recordAlertAcknowledgment(RiskAlert alert) {
        incrementCounter("risk.alerts.acknowledged",
                "type", alert.getAlertType().name(),
                "branch", alert.getBranchCode());

        log.debug("Recorded metrics for alert acknowledgment: {}", alert.getId());
    }

    public void recordAlertResolution(RiskAlert alert) {
        incrementCounter("risk.alerts.resolved",
                "type", alert.getAlertType().name(),
                "branch", alert.getBranchCode());

        log.debug("Recorded metrics for alert resolution: {}", alert.getId());
    }

    public void recordFalsePositiveAlert(RiskAlert alert) {
        incrementCounter("risk.alerts.false_positive",
                "type", alert.getAlertType().name(),
                "branch", alert.getBranchCode());

        log.debug("Recorded metrics for false positive alert: {}", alert.getId());
    }

    public void recordRiskCalculationTime(long duration, String branchCode, String currency) {
        recordTimer("risk.calculation.duration", duration,
                "branch", branchCode,
                "currency", currency);
    }

    private void incrementCounter(String name, String... tags) {
        String key = name + String.join("", tags);
        Counter counter = counters.computeIfAbsent(key,
                k -> Counter.builder(name)
                        .tags(tags)
                        .description("Risk service metrics")
                        .register(meterRegistry));
        counter.increment();
    }

    private void recordTimer(String name, long duration, String... tags) {
        String key = name + String.join("", tags);
        Timer timer = timers.computeIfAbsent(key,
                k -> Timer.builder(name)
                        .tags(tags)
                        .description("Risk service operation duration")
                        .register(meterRegistry));
        timer.record(duration, TimeUnit.MILLISECONDS);
    }
}