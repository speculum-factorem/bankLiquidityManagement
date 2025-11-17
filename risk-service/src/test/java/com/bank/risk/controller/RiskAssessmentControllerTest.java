package com.bank.risk.controller;

import com.bank.risk.dto.RiskAssessmentRequest;
import com.bank.risk.dto.RiskAssessmentResponse;
import com.bank.risk.service.RiskAssessmentService;
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

@WebMvcTest(RiskAssessmentController.class)
class RiskAssessmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RiskAssessmentService riskAssessmentService;

    @Test
    void shouldCreateRiskAssessment() throws Exception {
        RiskAssessmentRequest request = RiskAssessmentRequest.builder()
                .branchCode("NYC001")
                .currency("USD")
                .liquidityRisk(new BigDecimal("30.0"))
                .volatilityRisk(new BigDecimal("20.0"))
                .concentrationRisk(new BigDecimal("25.0"))
                .marketRisk(new BigDecimal("15.0"))
                .build();

        RiskAssessmentResponse response = RiskAssessmentResponse.builder()
                .id(1L)
                .branchCode("NYC001")
                .currency("USD")
                .riskScore(new BigDecimal("24.50"))
                .build();

        when(riskAssessmentService.createAssessment(any(RiskAssessmentRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/risk/assessments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.branchCode").value("NYC001"))
                .andExpect(jsonPath("$.data.currency").value("USD"));
    }

    @Test
    void shouldGetAllAssessments() throws Exception {
        RiskAssessmentResponse response = RiskAssessmentResponse.builder()
                .id(1L)
                .branchCode("NYC001")
                .currency("USD")
                .riskScore(new BigDecimal("24.50"))
                .build();

        when(riskAssessmentService.getAllAssessments()).thenReturn(List.of(response));

        mockMvc.perform(get("/api/risk/assessments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].branchCode").value("NYC001"))
                .andExpect(jsonPath("$.data[0].currency").value("USD"));
    }

    @Test
    void shouldValidateRequest() throws Exception {
        RiskAssessmentRequest invalidRequest = RiskAssessmentRequest.builder()
                .branchCode("NY") // Too short
                .currency("US") // Too short
                .liquidityRisk(new BigDecimal("150.0")) // Exceeds max
                .volatilityRisk(new BigDecimal("-10.0")) // Negative
                .concentrationRisk(null) // Required
                .marketRisk(null) // Required
                .build();

        mockMvc.perform(post("/api/risk/assessments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }
}