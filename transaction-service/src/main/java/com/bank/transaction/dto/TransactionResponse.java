package com.bank.transaction.dto;

import com.bank.transaction.model.Transaction;
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
public class TransactionResponse {
    private Long id;
    private String transactionId;
    private Transaction.TransactionType type;
    private BigDecimal amount;
    private String currency;
    private String accountNumber;
    private String counterpartyAccountNumber;
    private LocalDateTime transactionDate;
    private Transaction.TransactionStatus status;
    private String description;
    private String failureReason;
    private String branchCode;
    private String channel;
    private BigDecimal balanceAfter;
    private BigDecimal balanceBefore;
    private String referenceNumber;
    private String category;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean highValueTransaction;
    private boolean suspiciousTransaction;
}