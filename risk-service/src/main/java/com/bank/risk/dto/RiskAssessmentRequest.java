package com.bank.risk.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RiskAssessmentRequest {

    @NotBlank(message = "Branch code is required")
    @Size(min = 3, max = 10, message = "Branch code must be between 3 and 10 characters")
    private String branchCode;

    @NotBlank(message = "Currency is required")
    @Size(min = 3, max = 3, message = "Currency must be 3 characters")
    private String currency;

    @NotNull(message = "Liquidity risk score is required")
    @DecimalMin(value = "0.0", message = "Liquidity risk score cannot be negative")
    @DecimalMax(value = "100.0", message = "Liquidity risk score cannot exceed 100")
    private BigDecimal liquidityRisk;

    @NotNull(message = "Volatility risk score is required")
    @DecimalMin(value = "0.0", message = "Volatility risk score cannot be negative")
    @DecimalMax(value = "100.0", message = "Volatility risk score cannot exceed 100")
    private BigDecimal volatilityRisk;

    @NotNull(message = "Concentration risk score is required")
    @DecimalMin(value = "0.0", message = "Concentration risk score cannot be negative")
    @DecimalMax(value = "100.0", message = "Concentration risk score cannot exceed 100")
    private BigDecimal concentrationRisk;

    @NotNull(message = "Market risk score is required")
    @DecimalMin(value = "0.0", message = "Market risk score cannot be negative")
    @DecimalMax(value = "100.0", message = "Market risk score cannot exceed 100")
    private BigDecimal marketRisk;

    private String additionalFactors;
}