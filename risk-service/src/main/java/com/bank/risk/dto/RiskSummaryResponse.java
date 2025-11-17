package com.bank.risk.dto;

import com.bank.risk.model.RiskAssessment;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RiskSummaryResponse {
    private String branchCode;
    private String currency;
    private BigDecimal averageRiskScore;
    private RiskAssessment.RiskLevel currentRiskLevel;
    private Integer assessmentCount;
    private Map<RiskAssessment.RiskLevel, Long> riskLevelDistribution;
    private BigDecimal riskTrend; // Positive = increasing risk, Negative = decreasing risk
    private String summary;
}