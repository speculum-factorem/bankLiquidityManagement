package com.bank.liquidity.health;

import com.bank.liquidity.repository.LiquidityPositionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Slf4j
@Component
@RequiredArgsConstructor
public class LiquidityHealthIndicator implements HealthIndicator {

    private final LiquidityPositionRepository positionRepository;

    @Override
    public Health health() {
        try {
            long totalPositions = positionRepository.count();
            long negativePositions = positionRepository.findNegativeLiquidityPositions().size();
            long criticalPositions = positionRepository.countPositionsBelowLiquidityRatio(new BigDecimal("0.5"));

            Health.Builder status = totalPositions > 0 ? Health.up() : Health.unknown();

            return status
                    .withDetail("totalPositions", totalPositions)
                    .withDetail("negativePositions", negativePositions)
                    .withDetail("criticalPositions", criticalPositions)
                    .withDetail("healthPercentage", calculateHealthPercentage(totalPositions, negativePositions))
                    .build();

        } catch (Exception e) {
            log.error("Health check failed", e);
            return Health.down()
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }

    private double calculateHealthPercentage(long total, long negative) {
        if (total == 0) return 100.0;
        return ((double) (total - negative) / total) * 100;
    }
}