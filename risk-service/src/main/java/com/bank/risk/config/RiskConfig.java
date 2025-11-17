package com.bank.risk.config;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;

@Slf4j
@Data
@Configuration
@ConfigurationProperties(prefix = "risk")
public class RiskConfig {

    private RiskThresholds thresholds;
    private RiskWeights weights;
    private Alerting alerting;

    @Data
    public static class RiskThresholds {
        private BigDecimal lowRiskMax = new BigDecimal("25");
        private BigDecimal mediumRiskMax = new BigDecimal("50");
        private BigDecimal highRiskMax = new BigDecimal("75");
        private BigDecimal criticalRiskMin = new BigDecimal("75");

        private BigDecimal liquidityRatioCritical = new BigDecimal("0.5");
        private BigDecimal liquidityRatioHigh = new BigDecimal("1.0");
        private BigDecimal liquidityRatioMedium = new BigDecimal("1.5");
    }

    @Data
    public static class RiskWeights {
        private BigDecimal liquidityWeight = new BigDecimal("0.4");
        private BigDecimal volatilityWeight = new BigDecimal("0.3");
        private BigDecimal concentrationWeight = new BigDecimal("0.2");
        private BigDecimal marketWeight = new BigDecimal("0.1");
    }

    @Data
    public static class Alerting {
        private boolean enabled = true;
        private Integer highSeverityThreshold = 7;
        private Integer criticalSeverityThreshold = 9;
        private String notificationEmail;
    }

    @PostConstruct
    public void init() {
        log.info("Risk configuration loaded: {}", this);
    }
}