package com.bank.risk.controller;

import com.bank.risk.dto.ApiResponse;
import com.bank.risk.dto.RiskAssessmentRequest;
import com.bank.risk.dto.RiskAssessmentResponse;
import com.bank.risk.dto.RiskSummaryResponse;
import com.bank.risk.service.RiskAssessmentService;
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
public class RiskAssessmentController {

    private final RiskAssessmentService riskAssessmentService;

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

    @GetMapping
    public ResponseEntity<ApiResponse<List<RiskAssessmentResponse>>> getAllAssessments() {
        log.debug("Fetching all risk assessments");

        List<RiskAssessmentResponse> assessments = riskAssessmentService.getAllAssessments();

        log.debug("Retrieved {} risk assessments", assessments.size());

        return ResponseEntity.ok(ApiResponse.success(assessments));
    }

    @GetMapping("/branch/{branchCode}")
    public ResponseEntity<ApiResponse<List<RiskAssessmentResponse>>> getAssessmentsByBranch(
            @PathVariable String branchCode) {

        log.debug("Fetching risk assessments for branch: {}", branchCode);

        List<RiskAssessmentResponse> assessments = riskAssessmentService.getAssessmentsByBranch(branchCode);

        log.debug("Retrieved {} risk assessments for branch: {}", assessments.size(), branchCode);

        return ResponseEntity.ok(ApiResponse.success(assessments));
    }

    @GetMapping("/branch/{branchCode}/currency/{currency}/latest")
    public ResponseEntity<ApiResponse<RiskAssessmentResponse>> getLatestAssessment(
            @PathVariable String branchCode,
            @PathVariable String currency) {

        log.debug("Fetching latest risk assessment for branch: {}, currency: {}", branchCode, currency);

        return riskAssessmentService.getLatestAssessment(branchCode, currency)
                .map(assessment -> {
                    log.debug("Found latest assessment with score: {}", assessment.getRiskScore());
                    return ResponseEntity.ok(ApiResponse.success(assessment));
                })
                .orElse(ResponseEntity.ok(ApiResponse.error("No risk assessment found")));
    }

    @GetMapping("/high-risk")
    public ResponseEntity<ApiResponse<List<RiskAssessmentResponse>>> getHighRiskAssessments() {
        log.debug("Fetching high risk assessments");

        List<RiskAssessmentResponse> assessments = riskAssessmentService.getHighRiskAssessments();

        log.debug("Retrieved {} high risk assessments", assessments.size());

        return ResponseEntity.ok(ApiResponse.success(assessments));
    }

    @GetMapping("/critical-risk")
    public ResponseEntity<ApiResponse<List<RiskAssessmentResponse>>> getCriticalRiskAssessments() {
        log.debug("Fetching critical risk assessments");

        List<RiskAssessmentResponse> assessments = riskAssessmentService.getCriticalRiskAssessments();

        log.debug("Retrieved {} critical risk assessments", assessments.size());

        return ResponseEntity.ok(ApiResponse.success(assessments));
    }

    @GetMapping("/summary/branch/{branchCode}/currency/{currency}")
    public ResponseEntity<ApiResponse<RiskSummaryResponse>> getRiskSummary(
            @PathVariable String branchCode,
            @PathVariable String currency) {

        log.debug("Generating risk summary for branch: {}, currency: {}", branchCode, currency);

        RiskSummaryResponse summary = riskAssessmentService.getRiskSummary(branchCode, currency);

        log.debug("Risk summary generated for branch: {}", branchCode);

        return ResponseEntity.ok(ApiResponse.success(summary));
    }
}