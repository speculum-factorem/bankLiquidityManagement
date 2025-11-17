package com.bank.risk.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "risk_assessments", indexes = {
        @Index(name = "idx_branch_currency", columnList = "branchCode, currency"),
        @Index(name = "idx_assessment_date", columnList = "assessmentDate"),
        @Index(name = "idx_risk_score", columnList = "riskScore"),
        @Index(name = "idx_risk_level", columnList = "riskLevel")
})
public class RiskAssessment {

    public enum RiskLevel {
        LOW, MEDIUM, HIGH, CRITICAL
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 10)
    private String branchCode;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal riskScore;

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private RiskLevel riskLevel;

    @Column(nullable = false)
    private LocalDateTime assessmentDate;

    @Column(precision = 5, scale = 2)
    private BigDecimal liquidityRisk;

    @Column(precision = 5, scale = 2)
    private BigDecimal volatilityRisk;

    @Column(precision = 5, scale = 2)
    private BigDecimal concentrationRisk;

    @Column(precision = 5, scale = 2)
    private BigDecimal marketRisk;

    @Column(length = 500)
    private String recommendations;

    @Column(length = 1000)
    private String riskFactors;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Version
    private Long version;

    @PrePersist
    @PreUpdate
    private void calculateRiskLevel() {
        if (this.riskScore == null) {
            calculateOverallRiskScore();
        }

        if (this.riskScore != null) {
            if (this.riskScore.compareTo(new BigDecimal("25")) <= 0) {
                this.riskLevel = RiskLevel.LOW;
            } else if (this.riskScore.compareTo(new BigDecimal("50")) <= 0) {
                this.riskLevel = RiskLevel.MEDIUM;
            } else if (this.riskScore.compareTo(new BigDecimal("75")) <= 0) {
                this.riskLevel = RiskLevel.HIGH;
            } else {
                this.riskLevel = RiskLevel.CRITICAL;
            }
        }

        if (this.assessmentDate == null) {
            this.assessmentDate = LocalDateTime.now();
        }
    }

    private void calculateOverallRiskScore() {
        BigDecimal overallScore = BigDecimal.ZERO;
        int factorCount = 0;

        if (liquidityRisk != null) {
            overallScore = overallScore.add(liquidityRisk);
            factorCount++;
        }
        if (volatilityRisk != null) {
            overallScore = overallScore.add(volatilityRisk);
            factorCount++;
        }
        if (concentrationRisk != null) {
            overallScore = overallScore.add(concentrationRisk);
            factorCount++;
        }
        if (marketRisk != null) {
            overallScore = overallScore.add(marketRisk);
            factorCount++;
        }

        if (factorCount > 0) {
            this.riskScore = overallScore.divide(BigDecimal.valueOf(factorCount), 2, java.math.RoundingMode.HALF_UP);
        } else {
            this.riskScore = BigDecimal.ZERO;
        }
    }
}