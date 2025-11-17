package com.bank.transaction.model;

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
@Table(name = "transactions", indexes = {
        @Index(name = "idx_account_number", columnList = "accountNumber"),
        @Index(name = "idx_transaction_date", columnList = "transactionDate"),
        @Index(name = "idx_status", columnList = "status"),
        @Index(name = "idx_type", columnList = "type"),
        @Index(name = "idx_transaction_id", columnList = "transactionId", unique = true)
})
public class Transaction {

    public enum TransactionType {
        DEPOSIT, WITHDRAWAL, TRANSFER, PAYMENT, REFUND
    }

    public enum TransactionStatus {
        PENDING, PROCESSING, COMPLETED, FAILED, CANCELLED, REVERSED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 36)
    private String transactionId;

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private TransactionType type;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(nullable = false, length = 20)
    private String accountNumber;

    @Column(length = 20)
    private String counterpartyAccountNumber;

    @Column(nullable = false)
    private LocalDateTime transactionDate;

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private TransactionStatus status;

    @Column(length = 500)
    private String description;

    @Column(length = 1000)
    private String failureReason;

    @Column(length = 10)
    private String branchCode;

    @Column(length = 50)
    private String channel; // ONLINE, MOBILE, BRANCH, ATM

    @Column(precision = 15, scale = 2)
    private BigDecimal balanceAfter;

    @Column(precision = 15, scale = 2)
    private BigDecimal balanceBefore;

    @Column(length = 50)
    private String referenceNumber;

    @Column(length = 100)
    private String category; // SALARY, UTILITY, TRANSFER, etc.

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Version
    private Long version;

    @PrePersist
    private void prePersist() {
        if (this.transactionId == null) {
            this.transactionId = java.util.UUID.randomUUID().toString();
        }
        if (this.transactionDate == null) {
            this.transactionDate = LocalDateTime.now();
        }
        if (this.status == null) {
            this.status = TransactionStatus.PENDING;
        }
    }

    public boolean isHighValueTransaction() {
        return this.amount != null && this.amount.compareTo(new BigDecimal("10000")) >= 0;
    }

    public boolean isSuspiciousTransaction() {
        return this.amount != null && this.amount.compareTo(new BigDecimal("50000")) >= 0;
    }
}