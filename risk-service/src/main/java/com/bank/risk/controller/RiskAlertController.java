package com.bank.risk.controller;

import com.bank.risk.dto.ApiResponse;
import com.bank.risk.model.RiskAlert;
import com.bank.risk.service.RiskAlertService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/risk/alerts")
@RequiredArgsConstructor
public class RiskAlertController {

    private final RiskAlertService riskAlertService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<RiskAlert>>> getAllAlerts() {
        log.debug("Fetching all risk alerts");

        List<RiskAlert> alerts = riskAlertService.getAllAlerts();

        log.debug("Retrieved {} risk alerts", alerts.size());

        return ResponseEntity.ok(ApiResponse.success(alerts));
    }

    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<RiskAlert>>> getActiveAlerts() {
        log.debug("Fetching active risk alerts");

        List<RiskAlert> alerts = riskAlertService.getActiveAlerts();

        log.debug("Retrieved {} active risk alerts", alerts.size());

        return ResponseEntity.ok(ApiResponse.success(alerts));
    }

    @GetMapping("/branch/{branchCode}")
    public ResponseEntity<ApiResponse<List<RiskAlert>>> getAlertsByBranch(@PathVariable String branchCode) {
        log.debug("Fetching alerts for branch: {}", branchCode);

        List<RiskAlert> alerts = riskAlertService.getAlertsByBranch(branchCode);

        log.debug("Retrieved {} alerts for branch: {}", alerts.size(), branchCode);

        return ResponseEntity.ok(ApiResponse.success(alerts));
    }

    @GetMapping("/high-severity")
    public ResponseEntity<ApiResponse<List<RiskAlert>>> getHighSeverityAlerts(
            @RequestParam(defaultValue = "7") Integer minSeverity) {

        log.debug("Fetching high severity alerts (min severity: {})", minSeverity);

        List<RiskAlert> alerts = riskAlertService.getHighSeverityAlerts(minSeverity);

        log.debug("Retrieved {} high severity alerts", alerts.size());

        return ResponseEntity.ok(ApiResponse.success(alerts));
    }

    @GetMapping("/recent-critical")
    public ResponseEntity<ApiResponse<List<RiskAlert>>> getRecentCriticalAlerts() {
        log.debug("Fetching recent critical alerts");

        List<RiskAlert> alerts = riskAlertService.getRecentCriticalAlerts();

        log.debug("Retrieved {} recent critical alerts", alerts.size());

        return ResponseEntity.ok(ApiResponse.success(alerts));
    }

    @PostMapping("/{alertId}/acknowledge")
    public ResponseEntity<ApiResponse<RiskAlert>> acknowledgeAlert(
            @PathVariable Long alertId,
            @RequestParam String acknowledgedBy) {

        log.info("Acknowledging risk alert: {} by user: {}", alertId, acknowledgedBy);

        RiskAlert alert = riskAlertService.acknowledgeAlert(alertId, acknowledgedBy);

        log.info("Risk alert {} acknowledged successfully", alertId);

        return ResponseEntity.ok(ApiResponse.success(alert, "Risk alert acknowledged successfully"));
    }

    @PostMapping("/{alertId}/resolve")
    public ResponseEntity<ApiResponse<RiskAlert>> resolveAlert(
            @PathVariable Long alertId,
            @RequestParam String resolutionNotes) {

        log.info("Resolving risk alert: {}", alertId);

        RiskAlert alert = riskAlertService.resolveAlert(alertId, resolutionNotes);

        log.info("Risk alert {} resolved successfully", alertId);

        return ResponseEntity.ok(ApiResponse.success(alert, "Risk alert resolved successfully"));
    }

    @PostMapping("/{alertId}/false-positive")
    public ResponseEntity<ApiResponse<RiskAlert>> markAsFalsePositive(
            @PathVariable Long alertId,
            @RequestParam String notes) {

        log.info("Marking risk alert as false positive: {}", alertId);

        RiskAlert alert = riskAlertService.markAsFalsePositive(alertId, notes);

        log.info("Risk alert {} marked as false positive", alertId);

        return ResponseEntity.ok(ApiResponse.success(alert, "Risk alert marked as false positive"));
    }

    @GetMapping("/stats/active-count")
    public ResponseEntity<ApiResponse<Long>> getActiveAlertCount() {
        log.debug("Fetching active alert count");

        Long count = riskAlertService.getActiveAlertCount();

        log.debug("Active risk alert count: {}", count);

        return ResponseEntity.ok(ApiResponse.success(count));
    }

    @GetMapping("/stats/summary")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getAlertStatistics() {
        log.debug("Fetching alert statistics");

        Map<String, Long> statistics = riskAlertService.getAlertStatistics();

        log.debug("Retrieved alert statistics: {}", statistics);

        return ResponseEntity.ok(ApiResponse.success(statistics));
    }
}