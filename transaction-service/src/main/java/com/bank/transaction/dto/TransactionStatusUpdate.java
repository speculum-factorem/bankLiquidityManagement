package com.bank.transaction.dto;

import com.bank.transaction.model.Transaction;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionStatusUpdate {

    @NotNull(message = "Status is required")
    private Transaction.TransactionStatus status;

    private String failureReason;
}