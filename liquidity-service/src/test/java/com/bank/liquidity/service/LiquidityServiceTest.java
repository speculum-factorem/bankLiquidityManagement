package com.bank.liquidity.service;

import com.bank.liquidity.dto.LiquidityPositionRequest;
import com.bank.liquidity.dto.LiquidityPositionResponse;
import com.bank.liquidity.model.LiquidityPosition;
import com.bank.liquidity.repository.LiquidityPositionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LiquidityServiceTest {

    @Mock
    private LiquidityPositionRepository positionRepository;

    @Mock
    private LiquidityAlertRepository alertRepository;

    @Mock
    private LiquidityMetricsService metricsService;

    @InjectMocks
    private LiquidityService liquidityService;

    private LiquidityPositionRequest validRequest;
    private LiquidityPosition samplePosition;

    @BeforeEach
    void setUp() {
        validRequest = LiquidityPositionRequest.builder()
                .currency("USD")
                .availableCash(new BigDecimal("1000000.00"))
                .requiredReserves(new BigDecimal("800000.00"))
                .branchCode("NYC001")
                .build();

        samplePosition = LiquidityPosition.builder()
                .id(1L)
                .currency("USD")
                .availableCash(new BigDecimal("1000000.00"))
                .requiredReserves(new BigDecimal("800000.00"))
                .branchCode("NYC001")
                .build();
    }

    @Test
    void shouldCreateNewPositionWhenNotExists() {
        when(positionRepository.findByBranchCodeAndCurrency("NYC001", "USD"))
                .thenReturn(Optional.empty());
        when(positionRepository.save(any(LiquidityPosition.class)))
                .thenReturn(samplePosition);

        LiquidityPositionResponse response = liquidityService.createPosition(validRequest);

        assertNotNull(response);
        assertEquals("USD", response.getCurrency());
        assertEquals("NYC001", response.getBranchCode());
        assertEquals(new BigDecimal("200000.00"), response.getNetLiquidity());

        verify(positionRepository).save(any(LiquidityPosition.class));
        verify(metricsService).recordLiquidityPositionCreation(any(LiquidityPosition.class));
    }

    @Test
    void shouldUpdateExistingPosition() {
        when(positionRepository.findByBranchCodeAndCurrency("NYC001", "USD"))
                .thenReturn(Optional.of(samplePosition));
        when(positionRepository.save(any(LiquidityPosition.class)))
                .thenReturn(samplePosition);

        LiquidityPositionResponse response = liquidityService.createPosition(validRequest);

        assertNotNull(response);
        verify(positionRepository).save(samplePosition);
    }

    @Test
    void shouldGetAllPositions() {
        when(positionRepository.findAll()).thenReturn(List.of(samplePosition));

        List<LiquidityPositionResponse> responses = liquidityService.getAllPositions();

        assertFalse(responses.isEmpty());
        assertEquals(1, responses.size());
        assertEquals("USD", responses.get(0).getCurrency());
    }

    @Test
    void shouldGetPositionsByBranch() {
        when(positionRepository.findByBranchCode("NYC001")).thenReturn(List.of(samplePosition));

        List<LiquidityPositionResponse> responses = liquidityService.getPositionsByBranch("NYC001");

        assertFalse(responses.isEmpty());
        assertEquals(1, responses.size());
        assertEquals("NYC001", responses.get(0).getBranchCode());
    }
}