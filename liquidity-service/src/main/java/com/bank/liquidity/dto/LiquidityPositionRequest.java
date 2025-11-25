package com.bank.liquidity.dto;

import com.bank.liquidity.validation.ValidCurrency;
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
public class LiquidityPositionRequest {

    @NotBlank(message = "Currency is required")
    @Size(min = 3, max = 3, message = "Currency must be 3 characters")
    @ValidCurrency
    private String currency;

    @NotNull(message = "Available cash is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Available cash must be positive")
    private BigDecimal availableCash;

    @NotNull(message = "Required reserves is required")
    @DecimalMin(value = "0.0", message = "Required reserves cannot be negative")
    private BigDecimal requiredReserves;

    @NotBlank(message = "Branch code is required")
    @Size(min = 3, max = 10, message = "Branch code must be between 3 and 10 characters")
    private String branchCode;
}