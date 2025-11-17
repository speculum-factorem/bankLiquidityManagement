package com.bank.transaction.controller;

import com.bank.transaction.dto.ApiResponse;
import com.bank.transaction.model.TransactionAlert;
import com.bank.transaction.service.TransactionAlertService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/transactions/alerts")
@RequiredArgsConstructor
public class TransactionAlertController {

    private final TransactionAlertService alertService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<TransactionAlert>>> getAllAlerts() {
        log.debug("Fetching all transaction alerts");

        List<TransactionAlert> alerts = alertService.getAllAlerts();

        log.debug("Retrieved {} transaction alerts", alerts.size());

        return ResponseEntity.ok(ApiResponse.success(alerts));
    }

    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<TransactionAlert>>> getActiveAlerts() {
        log.debug("Fetching active transaction alerts");

        List<TransactionAlert> alerts = alertService.getActiveAlerts();

        log.debug("Retrieved {} active transaction alerts", alerts.size());

        return ResponseEntity.ok(ApiResponse.success(alerts));
    }

    @GetMapping("/account/{accountNumber}")
    public ResponseEntity<ApiResponse<List<TransactionAlert>>> getAlertsByAccount(@PathVariable String accountNumber) {
        log.debug("Fetching alerts for account: {}", accountNumber);

        List<TransactionAlert> alerts = alertService.getAlertsByAccount(accountNumber);

        log.debug("Retrieved {} alerts for account: {}", alerts.size(), accountNumber);

        return ResponseEntity.ok(ApiResponse.success(alerts));
    }

    @GetMapping("/high-severity")
    public ResponseEntity<ApiResponse<List<TransactionAlert>>> getHighSeverityAlerts(
            @RequestParam(defaultValue = "7") Integer minSeverity) {

        log.debug("Fetching high severity alerts (min severity: {})", minSeverity);

        List<TransactionAlert> alerts = alertService.getHighSeverityAlerts(minSeverity);

        log.debug("Retrieved {} high severity alerts", alerts.size());

        return ResponseEntity.ok(ApiResponse.success(alerts));
    }

    @GetMapping("/recent-high-severity")
    public ResponseEntity<ApiResponse<List<TransactionAlert>>> getRecentHighSeverityAlerts() {
        log.debug("Fetching recent high severity alerts");

        List<TransactionAlert> alerts = alertService.getRecentHighSeverityAlerts();

        log.debug("Retrieved {} recent high severity alerts", alerts.size());

        return ResponseEntity.ok(ApiResponse.success(alerts));
    }

    @PostMapping("/{alertId}/review")
    public ResponseEntity<ApiResponse<TransactionAlert>> reviewAlert(
            @PathVariable Long alertId,
            @RequestParam String reviewedBy,
            @RequestParam(required = false) String notes) {

        log.info("Reviewing alert: {} by user: {}", alertId, reviewedBy);

        TransactionAlert alert = alertService.reviewAlert(alertId, reviewedBy, notes);

        log.info("Alert {} reviewed successfully", alertId);

        return ResponseEntity.ok(ApiResponse.success(alert, "Alert reviewed successfully"));
    }

    @PostMapping("/{alertId}/resolve")
    public ResponseEntity<ApiResponse<TransactionAlert>> resolveAlert(
            @PathVariable Long alertId,
            @RequestParam String resolvedBy,
            @RequestParam String resolutionNotes) {

        log.info("Resolving alert: {}", alertId);

        TransactionAlert alert = alertService.resolveAlert(alertId, resolvedBy, resolutionNotes);

        log.info("Alert {} resolved successfully", alertId);

        return ResponseEntity.ok(ApiResponse.success(alert, "Alert resolved successfully"));
    }

    @PostMapping("/{alertId}/false-positive")
    public ResponseEntity<ApiResponse<TransactionAlert>> markAsFalsePositive(
            @PathVariable Long alertId,
            @RequestParam String markedBy,
            @RequestParam String notes) {

        log.info("Marking alert as false positive: {}", alertId);

        TransactionAlert alert = alertService.markAsFalsePositive(alertId, markedBy, notes);

        log.info("Alert {} marked as false positive", alertId);

        return ResponseEntity.ok(ApiResponse.success(alert, "Alert marked as false positive"));
    }

    @GetMapping("/stats/active-count")
    public ResponseEntity<ApiResponse<Long>> getActiveAlertCount() {
        log.debug("Fetching active alert count");

        Long count = alertService.getActiveAlertCount();

        log.debug("Active transaction alert count: {}", count);

        return ResponseEntity.ok(ApiResponse.success(count));
    }

    @GetMapping("/stats/summary")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getAlertStatistics() {
        log.debug("Fetching alert statistics");

        Map<String, Long> statistics = alertService.getAlertStatistics();

        log.debug("Retrieved alert statistics: {}", statistics);

        return ResponseEntity.ok(ApiResponse.success(statistics));
    }

    @GetMapping("/stats/status-distribution")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getAlertStatusDistribution() {
        log.debug("Fetching alert status distribution");

        Map<String, Long> distribution = alertService.getAlertStatusDistribution();

        log.debug("Retrieved alert status distribution: {}", distribution);

        return ResponseEntity.ok(ApiResponse.success(distribution));
    }
}