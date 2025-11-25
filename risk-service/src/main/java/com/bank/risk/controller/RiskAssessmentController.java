package com.bank.risk.controller;

import com.bank.risk.dto.ApiResponse;
import com.bank.risk.dto.RiskAssessmentRequest;
import com.bank.risk.dto.RiskAssessmentResponse;
import com.bank.risk.dto.RiskSummaryResponse;
import com.bank.risk.service.RiskAssessmentService;
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

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/risk/assessments")
@RequiredArgsConstructor
@Tag(name = "Risk Assessment", description = "API для оценки и управления рисками банка")
public class RiskAssessmentController {

    private final RiskAssessmentService riskAssessmentService;

    @Operation(summary = "Создать оценку риска", 
               description = "Создает новую оценку риска для филиала и валюты")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Оценка риска успешно создана"),
        @ApiResponse(responseCode = "400", description = "Неверные входные данные"),
        @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
    })
    @PostMapping
    public ResponseEntity<ApiResponse<RiskAssessmentResponse>> createAssessment(
            @Valid @RequestBody RiskAssessmentRequest request) {

        log.info("Creating risk assessment for branch: {}", request.getBranchCode());

        long startTime = System.currentTimeMillis();
        try {
            RiskAssessmentResponse response = riskAssessmentService.createAssessment(request);

            log.info("Risk assessment created successfully for branch: {}", request.getBranchCode());

            return ResponseEntity.ok(ApiResponse.success(response, "Risk assessment created successfully"));

        } finally {
            long duration = System.currentTimeMillis() - startTime;
            log.debug("Risk assessment creation completed in {}ms", duration);
        }
    }

    @Operation(summary = "Получить все оценки риска", 
               description = "Возвращает все оценки риска по всем филиалам")
    @GetMapping
    public ResponseEntity<ApiResponse<List<RiskAssessmentResponse>>> getAllAssessments() {
        log.debug("Fetching all risk assessments");

        List<RiskAssessmentResponse> assessments = riskAssessmentService.getAllAssessments();

        log.debug("Retrieved {} risk assessments", assessments.size());

        return ResponseEntity.ok(ApiResponse.success(assessments));
    }

    @Operation(summary = "Получить оценки риска по филиалу", 
               description = "Возвращает все оценки риска для указанного филиала")
    @GetMapping("/branch/{branchCode}")
    public ResponseEntity<ApiResponse<List<RiskAssessmentResponse>>> getAssessmentsByBranch(
            @Parameter(description = "Код филиала") @PathVariable String branchCode) {

        log.debug("Fetching risk assessments for branch: {}", branchCode);

        List<RiskAssessmentResponse> assessments = riskAssessmentService.getAssessmentsByBranch(branchCode);

        log.debug("Retrieved {} risk assessments for branch: {}", assessments.size(), branchCode);

        return ResponseEntity.ok(ApiResponse.success(assessments));
    }

    @Operation(summary = "Получить последнюю оценку риска", 
               description = "Возвращает последнюю оценку риска для филиала и валюты")
    @GetMapping("/branch/{branchCode}/currency/{currency}/latest")
    public ResponseEntity<ApiResponse<RiskAssessmentResponse>> getLatestAssessment(
            @Parameter(description = "Код филиала") @PathVariable String branchCode,
            @Parameter(description = "Валюта") @PathVariable String currency) {

        log.debug("Fetching latest risk assessment for branch: {}, currency: {}", branchCode, currency);

        return riskAssessmentService.getLatestAssessment(branchCode, currency)
                .map(assessment -> {
                    log.debug("Found latest assessment with score: {}", assessment.getRiskScore());
                    return ResponseEntity.ok(ApiResponse.success(assessment));
                })
                .orElse(ResponseEntity.ok(ApiResponse.error("No risk assessment found")));
    }

    @Operation(summary = "Получить оценки высокого риска", 
               description = "Возвращает все оценки с высоким уровнем риска")
    @GetMapping("/high-risk")
    public ResponseEntity<ApiResponse<List<RiskAssessmentResponse>>> getHighRiskAssessments() {
        log.debug("Fetching high risk assessments");

        List<RiskAssessmentResponse> assessments = riskAssessmentService.getHighRiskAssessments();

        log.debug("Retrieved {} high risk assessments", assessments.size());

        return ResponseEntity.ok(ApiResponse.success(assessments));
    }

    @Operation(summary = "Получить критические оценки риска", 
               description = "Возвращает все оценки с критическим уровнем риска")
    @GetMapping("/critical-risk")
    public ResponseEntity<ApiResponse<List<RiskAssessmentResponse>>> getCriticalRiskAssessments() {
        log.debug("Fetching critical risk assessments");

        List<RiskAssessmentResponse> assessments = riskAssessmentService.getCriticalRiskAssessments();

        log.debug("Retrieved {} critical risk assessments", assessments.size());

        return ResponseEntity.ok(ApiResponse.success(assessments));
    }

    @Operation(summary = "Получить сводку по рискам", 
               description = "Возвращает сводную информацию по рискам для филиала и валюты")
    @GetMapping("/summary/branch/{branchCode}/currency/{currency}")
    public ResponseEntity<ApiResponse<RiskSummaryResponse>> getRiskSummary(
            @Parameter(description = "Код филиала") @PathVariable String branchCode,
            @Parameter(description = "Валюта") @PathVariable String currency) {

        log.debug("Generating risk summary for branch: {}, currency: {}", branchCode, currency);

        RiskSummaryResponse summary = riskAssessmentService.getRiskSummary(branchCode, currency);

        log.debug("Risk summary generated for branch: {}", branchCode);

        return ResponseEntity.ok(ApiResponse.success(summary));
    }
}