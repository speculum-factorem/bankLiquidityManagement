package com.bank.transaction.model;

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
@Table(name = "transaction_alerts")
public class TransactionAlert {

    public enum AlertType {
        HIGH_VALUE_TRANSACTION,
        SUSPICIOUS_ACTIVITY,
        MULTIPLE_FAILED_ATTEMPTS,
        UNUSUAL_PATTERN,
        FRAUD_SUSPICION
    }

    public enum AlertStatus {
        ACTIVE, UNDER_REVIEW, RESOLVED, FALSE_POSITIVE
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

    @Column(nullable = false, length = 36)
    private String transactionId;

    @Column(nullable = false, length = 20)
    private String accountNumber;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(nullable = false)
    private String message;

    @Column(nullable = false)
    private Integer severity; // 1-10, where 10 is most severe

    @Column(length = 1000)
    private String details;

    @Column(length = 500)
    private String investigationNotes;

    @CreationTimestamp
    private LocalDateTime createdAt;

    private LocalDateTime reviewedAt;

    private LocalDateTime resolvedAt;

    private String reviewedBy;

    private String resolvedBy;

    @Column(length = 1000)
    private String resolutionNotes;
}