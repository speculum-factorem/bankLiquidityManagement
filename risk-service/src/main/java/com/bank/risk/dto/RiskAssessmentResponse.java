package com.bank.risk.dto;

import com.bank.risk.model.RiskAssessment;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RiskAssessmentResponse {
    private Long id;
    private String branchCode;
    private String currency;
    private BigDecimal riskScore;
    private RiskAssessment.RiskLevel riskLevel;
    private LocalDateTime assessmentDate;
    private BigDecimal liquidityRisk;
    private BigDecimal volatilityRisk;
    private BigDecimal concentrationRisk;
    private BigDecimal marketRisk;
    private String recommendations;
    private String riskFactors;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}