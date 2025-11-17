package com.bank.liquidity.controller;

import com.bank.liquidity.dto.LiquidityPositionRequest;
import com.bank.liquidity.dto.LiquidityPositionResponse;
import com.bank.liquidity.service.LiquidityService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(LiquidityController.class)
class LiquidityControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private LiquidityService liquidityService;

    @Test
    void shouldCreatePosition() throws Exception {
        LiquidityPositionRequest request = LiquidityPositionRequest.builder()
                .currency("USD")
                .availableCash(new BigDecimal("1000000.00"))
                .requiredReserves(new BigDecimal("800000.00"))
                .branchCode("NYC001")
                .build();

        LiquidityPositionResponse response = LiquidityPositionResponse.builder()
                .id(1L)
                .currency("USD")
                .availableCash(new BigDecimal("1000000.00"))
                .requiredReserves(new BigDecimal("800000.00"))
                .netLiquidity(new BigDecimal("200000.00"))
                .branchCode("NYC001")
                .build();

        when(liquidityService.createPosition(any(LiquidityPositionRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/liquidity/positions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.currency").value("USD"))
                .andExpect(jsonPath("$.data.branchCode").value("NYC001"));
    }

    @Test
    void shouldGetAllPositions() throws Exception {
        LiquidityPositionResponse response = LiquidityPositionResponse.builder()
                .id(1L)
                .currency("USD")
                .branchCode("NYC001")
                .build();

        when(liquidityService.getAllPositions()).thenReturn(List.of(response));

        mockMvc.perform(get("/api/liquidity/positions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].currency").value("USD"))
                .andExpect(jsonPath("$.data[0].branchCode").value("NYC001"));
    }

    @Test
    void shouldValidateRequest() throws Exception {
        LiquidityPositionRequest invalidRequest = LiquidityPositionRequest.builder()
                .currency("US") // Too short
                .availableCash(BigDecimal.ZERO) // Must be positive
                .requiredReserves(new BigDecimal("-100")) // Cannot be negative
                .branchCode("A") // Too short
                .build();

        mockMvc.perform(post("/api/liquidity/positions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }
}