package com.bank.liquidity.service;

import com.bank.liquidity.model.LiquidityAlert;
import com.bank.liquidity.model.LiquidityPosition;
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
public class LiquidityMetricsService {

    private final MeterRegistry meterRegistry;
    private final ConcurrentHashMap<String, Counter> counters = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Timer> timers = new ConcurrentHashMap<>();

    public void recordLiquidityPositionCreation(LiquidityPosition position) {
        incrementCounter("liquidity.positions.created",
                "branch", position.getBranchCode(),
                "currency", position.getCurrency(),
                "status", position.getStatus());

        // Record liquidity ratio
        meterRegistry.gauge("liquidity.ratio",
                List.of(
                        meterRegistry.config().tag("branch", position.getBranchCode()),
                        meterRegistry.config().tag("currency", position.getCurrency())
                ),
                position.getLiquidityRatio(),
                BigDecimal::doubleValue);

        // Record net liquidity
        meterRegistry.gauge("liquidity.net",
                List.of(
                        meterRegistry.config().tag("branch", position.getBranchCode()),
                        meterRegistry.config().tag("currency", position.getCurrency())
                ),
                position.getNetLiquidity(),
                BigDecimal::doubleValue);

        log.debug("Recorded metrics for liquidity position: {} (branch: {}, currency: {})",
                position.getId(), position.getBranchCode(), position.getCurrency());
    }

    public void recordLiquidityAlert(LiquidityAlert alert) {
        incrementCounter("liquidity.alerts.created",
                "type", alert.getAlertType().name(),
                "branch", alert.getBranchCode(),
                "currency", alert.getCurrency(),
                "severity", alert.getSeverity().toString());

        log.debug("Recorded metrics for liquidity alert: {} (type: {}, severity: {})",
                alert.getId(), alert.getAlertType(), alert.getSeverity());
    }

    public void recordAlertAcknowledgment(LiquidityAlert alert) {
        incrementCounter("liquidity.alerts.acknowledged",
                "type", alert.getAlertType().name(),
                "branch", alert.getBranchCode());

        log.debug("Recorded metrics for alert acknowledgment: {}", alert.getId());
    }

    public void recordAlertResolution(LiquidityAlert alert) {
        incrementCounter("liquidity.alerts.resolved",
                "type", alert.getAlertType().name(),
                "branch", alert.getBranchCode());

        log.debug("Recorded metrics for alert resolution: {}", alert.getId());
    }

    public void recordLiquidityCalculationTime(long duration, String branchCode, String currency) {
        recordTimer("liquidity.calculation.duration", duration,
                "branch", branchCode,
                "currency", currency);
    }

    private void incrementCounter(String name, String... tags) {
        String key = name + String.join("", tags);
        Counter counter = counters.computeIfAbsent(key,
                k -> Counter.builder(name)
                        .tags(tags)
                        .description("Liquidity service metrics")
                        .register(meterRegistry));
        counter.increment();
    }

    private void recordTimer(String name, long duration, String... tags) {
        String key = name + String.join("", tags);
        Timer timer = timers.computeIfAbsent(key,
                k -> Timer.builder(name)
                        .tags(tags)
                        .description("Liquidity service operation duration")
                        .register(meterRegistry));
        timer.record(duration, TimeUnit.MILLISECONDS);
    }
}