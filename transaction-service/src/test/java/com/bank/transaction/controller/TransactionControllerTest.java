package com.bank.transaction.controller;

import com.bank.transaction.dto.TransactionRequest;
import com.bank.transaction.dto.TransactionResponse;
import com.bank.transaction.model.Transaction;
import com.bank.transaction.service.TransactionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TransactionController.class)
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TransactionService transactionService;

    @Test
    void shouldCreateTransaction() throws Exception {
        TransactionRequest request = TransactionRequest.builder()
                .type(Transaction.TransactionType.DEPOSIT)
                .amount(new BigDecimal("1000.00"))
                .currency("USD")
                .accountNumber("1234567890")
                .description("Test deposit")
                .build();

        TransactionResponse response = TransactionResponse.builder()
                .id(1L)
                .transactionId("test-transaction-id")
                .type(Transaction.TransactionType.DEPOSIT)
                .amount(new BigDecimal("1000.00"))
                .currency("USD")
                .accountNumber("1234567890")
                .status(Transaction.TransactionStatus.PENDING)
                .build();

        when(transactionService.createTransaction(any(TransactionRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.transactionId").value("test-transaction-id"))
                .andExpect(jsonPath("$.data.accountNumber").value("1234567890"));
    }

    @Test
    void shouldGetTransactionById() throws Exception {
        TransactionResponse response = TransactionResponse.builder()
                .id(1L)
                .transactionId("test-transaction-id")
                .accountNumber("1234567890")
                .build();

        when(transactionService.getTransactionById(1L)).thenReturn(Optional.of(response));

        mockMvc.perform(get("/api/transactions/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.transactionId").value("test-transaction-id"));
    }

    @Test
    void shouldGetTransactionsByAccount() throws Exception {
        TransactionResponse response = TransactionResponse.builder()
                .id(1L)
                .transactionId("test-transaction-id")
                .accountNumber("1234567890")
                .build();

        when(transactionService.getTransactionsByAccount("1234567890")).thenReturn(List.of(response));

        mockMvc.perform(get("/api/transactions/account/1234567890"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].accountNumber").value("1234567890"));
    }

    @Test
    void shouldValidateRequest() throws Exception {
        TransactionRequest invalidRequest = TransactionRequest.builder()
                .type(null) // Required
                .amount(new BigDecimal("-100.00")) // Must be positive
                .currency("US") // Too short
                .accountNumber("123") // Too short
                .build();

        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }
}