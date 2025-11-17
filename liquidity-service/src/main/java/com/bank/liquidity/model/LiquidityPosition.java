package com.bank.liquidity.model;

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
@Table(name = "liquidity_positions", indexes = {
        @Index(name = "idx_branch_currency", columnList = "branchCode, currency"),
        @Index(name = "idx_calculation_date", columnList = "calculationDate"),
        @Index(name = "idx_net_liquidity", columnList = "netLiquidity")
})
public class LiquidityPosition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal availableCash;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal requiredReserves;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal netLiquidity;

    @Column(nullable = false)
    private LocalDateTime calculationDate;

    @Column(nullable = false, length = 10)
    private String branchCode;

    @Column(length = 50)
    private String status;

    @Column(precision = 5, scale = 2)
    private BigDecimal liquidityRatio;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Version
    private Long version;

    @PrePersist
    @PreUpdate
    private void calculateDerivedFields() {
        if (availableCash != null && requiredReserves != null) {
            this.netLiquidity = availableCash.subtract(requiredReserves);

            if (requiredReserves.compareTo(BigDecimal.ZERO) > 0) {
                this.liquidityRatio = availableCash.divide(requiredReserves, 4, java.math.RoundingMode.HALF_UP);
            } else {
                this.liquidityRatio = BigDecimal.valueOf(100); // Infinite ratio if no reserves required
            }

            if (this.status == null) {
                this.status = netLiquidity.compareTo(BigDecimal.ZERO) >= 0 ? "HEALTHY" : "DEFICIT";
            }
        }

        if (this.calculationDate == null) {
            this.calculationDate = LocalDateTime.now();
        }
    }
}