package com.bank.transaction.service;

import com.bank.transaction.dto.TransactionRequest;
import com.bank.transaction.dto.TransactionResponse;
import com.bank.transaction.model.Transaction;
import com.bank.transaction.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private TransactionAlertRepository alertRepository;

    @Mock
    private TransactionMetricsService metricsService;

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @InjectMocks
    private TransactionService transactionService;

    private TransactionRequest validRequest;
    private Transaction sampleTransaction;

    @BeforeEach
    void setUp() {
        validRequest = TransactionRequest.builder()
                .type(Transaction.TransactionType.DEPOSIT)
                .amount(new BigDecimal("1000.00"))
                .currency("USD")
                .accountNumber("1234567890")
                .description("Test deposit")
                .build();

        sampleTransaction = Transaction.builder()
                .id(1L)
                .transactionId("test-transaction-id")
                .type(Transaction.TransactionType.DEPOSIT)
                .amount(new BigDecimal("1000.00"))
                .currency("USD")
                .accountNumber("1234567890")
                .status(Transaction.TransactionStatus.PENDING)
                .build();
    }

    @Test
    void shouldCreateTransaction() {
        when(transactionRepository.save(any(Transaction.class))).thenReturn(sampleTransaction);

        TransactionResponse response = transactionService.createTransaction(validRequest);

        assertNotNull(response);
        assertEquals("test-transaction-id", response.getTransactionId());
        assertEquals("USD", response.getCurrency());
        assertEquals("1234567890", response.getAccountNumber());

        verify(transactionRepository).save(any(Transaction.class));
        verify(metricsService).recordTransactionCreation(any(Transaction.class));
    }

    @Test
    void shouldGetTransactionById() {
        when(transactionRepository.findById(1L)).thenReturn(Optional.of(sampleTransaction));

        Optional<TransactionResponse> response = transactionService.getTransactionById(1L);

        assertTrue(response.isPresent());
        assertEquals("test-transaction-id", response.get().getTransactionId());
    }

    @Test
    void shouldGetTransactionsByAccount() {
        when(transactionRepository.findByAccountNumber("1234567890")).thenReturn(List.of(sampleTransaction));

        List<TransactionResponse> responses = transactionService.getTransactionsByAccount("1234567890");

        assertFalse(responses.isEmpty());
        assertEquals(1, responses.size());
        assertEquals("1234567890", responses.get(0).getAccountNumber());
    }

    @Test
    void shouldCreateHighValueTransactionAlert() {
        Transaction highValueTransaction = Transaction.builder()
                .id(2L)
                .transactionId("high-value-tx")
                .type(Transaction.TransactionType.TRANSFER)
                .amount(new BigDecimal("15000.00"))
                .currency("USD")
                .accountNumber("1234567890")
                .status(Transaction.TransactionStatus.PENDING)
                .build();

        when(transactionRepository.save(any(Transaction.class))).thenReturn(highValueTransaction);
        when(alertRepository.save(any(TransactionAlert.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TransactionRequest highValueRequest = TransactionRequest.builder()
                .type(Transaction.TransactionType.TRANSFER)
                .amount(new BigDecimal("15000.00"))
                .currency("USD")
                .accountNumber("1234567890")
                .build();

        TransactionResponse response = transactionService.createTransaction(highValueRequest);

        assertNotNull(response);
        assertTrue(response.isHighValueTransaction());
        verify(alertRepository).save(any(TransactionAlert.class));
    }
}