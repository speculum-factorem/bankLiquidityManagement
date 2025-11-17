package com.bank.liquidity.dto;

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
public class LiquidityPositionResponse {
    private Long id;
    private String currency;
    private BigDecimal availableCash;
    private BigDecimal requiredReserves;
    private BigDecimal netLiquidity;
    private BigDecimal liquidityRatio;
    private LocalDateTime calculationDate;
    private String branchCode;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}