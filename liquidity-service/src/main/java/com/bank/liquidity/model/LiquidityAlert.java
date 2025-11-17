package com.bank.liquidity.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "liquidity_alerts")
public class LiquidityAlert {

    public enum AlertType {
        DEFICIT, LOW_LIQUIDITY, CRITICAL, THRESHOLD_BREACH
    }

    public enum AlertStatus {
        ACTIVE, ACKNOWLEDGED, RESOLVED
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

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal deficitAmount;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal liquidityRatio;

    @Column(nullable = false)
    private String message;

    @Column(nullable = false)
    private Integer severity; // 1-10, where 10 is most severe

    @CreationTimestamp
    private LocalDateTime createdAt;

    private LocalDateTime acknowledgedAt;

    private LocalDateTime resolvedAt;

    private String acknowledgedBy;

    private String resolutionNotes;
}