package com.bank.risk.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "risk_alerts")
public class RiskAlert {

    public enum AlertType {
        RISK_THRESHOLD_BREACH,
        RISK_INCREASE,
        CONCENTRATION_RISK,
        LIQUIDITY_RISK,
        MARKET_VOLATILITY
    }

    public enum AlertStatus {
        ACTIVE, ACKNOWLEDGED, RESOLVED, FALSE_POSITIVE
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private AlertType alertType;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private AlertStatus status;

    @Column(nullable = false, length = 10)
    private String branchCode;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal riskScore;

    @Column(nullable = false, length = 20)
    private String riskLevel;

    @Column(nullable = false)
    private String message;

    @Column(nullable = false)
    private Integer severity; // 1-10, where 10 is most severe

    @Column(length = 1000)
    private String details;

    @Column(length = 500)
    private String mitigationSteps;

    @CreationTimestamp
    private LocalDateTime createdAt;

    private LocalDateTime acknowledgedAt;

    private LocalDateTime resolvedAt;

    private String acknowledgedBy;

    private String resolutionNotes;

    @Column(precision = 5, scale = 2)
    private BigDecimal previousRiskScore;
}