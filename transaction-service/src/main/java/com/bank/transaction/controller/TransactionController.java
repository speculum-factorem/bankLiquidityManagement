package com.bank.transaction.controller;

import com.bank.transaction.dto.ApiResponse;
import com.bank.transaction.dto.TransactionRequest;
import com.bank.transaction.dto.TransactionResponse;
import com.bank.transaction.dto.TransactionStatusUpdate;
import com.bank.transaction.model.Transaction;
import com.bank.transaction.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
@Tag(name = "Transaction Management", description = "API для управления банковскими транзакциями")
public class TransactionController {

    private final TransactionService transactionService;

    @Operation(summary = "Создать транзакцию", 
               description = "Создает новую банковскую транзакцию")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Транзакция успешно создана"),
        @ApiResponse(responseCode = "400", description = "Неверные входные данные"),
        @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
    })
    @PostMapping
    public ResponseEntity<ApiResponse<TransactionResponse>> createTransaction(
            @Valid @RequestBody TransactionRequest request) {

        log.info("Creating transaction for account: {}", request.getAccountNumber());

        long startTime = System.currentTimeMillis();
        try {
            TransactionResponse response = transactionService.createTransaction(request);

            log.info("Transaction created successfully: {} for account: {}",
                    response.getTransactionId(), response.getAccountNumber());

            return ResponseEntity.ok(ApiResponse.success(response, "Transaction created successfully"));

        } finally {
            long duration = System.currentTimeMillis() - startTime;
            log.debug("Transaction creation completed in {}ms", duration);
        }
    }

    @Operation(summary = "Получить все транзакции", 
               description = "Возвращает список всех транзакций")
    @GetMapping
    public ResponseEntity<ApiResponse<List<TransactionResponse>>> getAllTransactions() {
        log.debug("Fetching all transactions");

        List<TransactionResponse> transactions = transactionService.getAllTransactions();

        log.debug("Retrieved {} transactions", transactions.size());

        return ResponseEntity.ok(ApiResponse.success(transactions));
    }

    @Operation(summary = "Получить транзакцию по ID", 
               description = "Возвращает транзакцию по внутреннему идентификатору")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TransactionResponse>> getTransactionById(
            @Parameter(description = "Внутренний ID транзакции") @PathVariable Long id) {
        log.debug("Fetching transaction by id: {}", id);

        return transactionService.getTransactionById(id)
                .map(transaction -> {
                    log.debug("Found transaction: {}", transaction.getTransactionId());
                    return ResponseEntity.ok(ApiResponse.success(transaction));
                })
                .orElse(ResponseEntity.ok(ApiResponse.error("Transaction not found")));
    }

    @GetMapping("/transaction-id/{transactionId}")
    public ResponseEntity<ApiResponse<TransactionResponse>> getTransactionByTransactionId(
            @PathVariable String transactionId) {

        log.debug("Fetching transaction by transactionId: {}", transactionId);

        return transactionService.getTransactionByTransactionId(transactionId)
                .map(transaction -> {
                    log.debug("Found transaction: {}", transaction.getTransactionId());
                    return ResponseEntity.ok(ApiResponse.success(transaction));
                })
                .orElse(ResponseEntity.ok(ApiResponse.error("Transaction not found")));
    }

    @GetMapping("/account/{accountNumber}")
    public ResponseEntity<ApiResponse<List<TransactionResponse>>> getTransactionsByAccount(
            @PathVariable String accountNumber) {

        log.debug("Fetching transactions for account: {}", accountNumber);

        List<TransactionResponse> transactions = transactionService.getTransactionsByAccount(accountNumber);

        log.debug("Retrieved {} transactions for account: {}", transactions.size(), accountNumber);

        return ResponseEntity.ok(ApiResponse.success(transactions));
    }

    @GetMapping("/account/{accountNumber}/recent")
    public ResponseEntity<ApiResponse<List<TransactionResponse>>> getRecentTransactionsByAccount(
            @PathVariable String accountNumber,
            @RequestParam(defaultValue = "10") int limit) {

        log.debug("Fetching recent {} transactions for account: {}", limit, accountNumber);

        List<TransactionResponse> transactions = transactionService.getRecentTransactionsByAccount(accountNumber, limit);

        log.debug("Retrieved {} recent transactions for account: {}", transactions.size(), accountNumber);

        return ResponseEntity.ok(ApiResponse.success(transactions));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<ApiResponse<List<TransactionResponse>>> getTransactionsByStatus(
            @PathVariable Transaction.TransactionStatus status) {

        log.debug("Fetching transactions with status: {}", status);

        List<TransactionResponse> transactions = transactionService.getTransactionsByStatus(status);

        log.debug("Retrieved {} transactions with status: {}", transactions.size(), status);

        return ResponseEntity.ok(ApiResponse.success(transactions));
    }

    @GetMapping("/high-value")
    public ResponseEntity<ApiResponse<List<TransactionResponse>>> getHighValueTransactions(
            @RequestParam(defaultValue = "10000") BigDecimal minAmount) {

        log.debug("Fetching high value transactions with min amount: {}", minAmount);

        List<TransactionResponse> transactions = transactionService.getHighValueTransactions(minAmount);

        log.debug("Retrieved {} high value transactions", transactions.size());

        return ResponseEntity.ok(ApiResponse.success(transactions));
    }

    @Operation(summary = "Обновить статус транзакции", 
               description = "Обновляет статус транзакции по внутреннему ID")
    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse<TransactionResponse>> updateTransactionStatus(
            @Parameter(description = "Внутренний ID транзакции") @PathVariable Long id,
            @Valid @RequestBody TransactionStatusUpdate statusUpdate) {

        log.info("Updating transaction status for id: {} to {}", id, statusUpdate.getStatus());

        TransactionResponse response = transactionService.updateTransactionStatus(id, statusUpdate);

        log.info("Transaction status updated successfully: {}", response.getTransactionId());

        return ResponseEntity.ok(ApiResponse.success(response, "Transaction status updated successfully"));
    }

    @PutMapping("/transaction-id/{transactionId}/status")
    public ResponseEntity<ApiResponse<TransactionResponse>> updateTransactionStatusByTransactionId(
            @PathVariable String transactionId,
            @Valid @RequestBody TransactionStatusUpdate statusUpdate) {

        log.info("Updating transaction status for transactionId: {} to {}", transactionId, statusUpdate.getStatus());

        TransactionResponse response = transactionService.updateTransactionStatusByTransactionId(transactionId, statusUpdate);

        log.info("Transaction status updated successfully: {}", response.getTransactionId());

        return ResponseEntity.ok(ApiResponse.success(response, "Transaction status updated successfully"));
    }
}