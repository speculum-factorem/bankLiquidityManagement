package com.bank.liquidity.controller;

import com.bank.liquidity.dto.ApiResponse;
import com.bank.liquidity.model.LiquidityAlert;
import com.bank.liquidity.service.LiquidityAlertService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/liquidity/alerts")
@RequiredArgsConstructor
public class LiquidityAlertController {

    private final LiquidityAlertService alertService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<LiquidityAlert>>> getAllAlerts() {
        log.debug("Fetching all liquidity alerts");

        List<LiquidityAlert> alerts = alertService.getAllAlerts();

        log.debug("Retrieved {} liquidity alerts", alerts.size());

        return ResponseEntity.ok(ApiResponse.success(alerts));
    }

    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<LiquidityAlert>>> getActiveAlerts() {
        log.debug("Fetching active liquidity alerts");

        List<LiquidityAlert> alerts = alertService.getActiveAlerts();

        log.debug("Retrieved {} active liquidity alerts", alerts.size());

        return ResponseEntity.ok(ApiResponse.success(alerts));
    }

    @GetMapping("/branch/{branchCode}")
    public ResponseEntity<ApiResponse<List<LiquidityAlert>>> getAlertsByBranch(@PathVariable String branchCode) {
        log.debug("Fetching alerts for branch: {}", branchCode);

        List<LiquidityAlert> alerts = alertService.getAlertsByBranch(branchCode);

        log.debug("Retrieved {} alerts for branch: {}", alerts.size(), branchCode);

        return ResponseEntity.ok(ApiResponse.success(alerts));
    }

    @GetMapping("/high-severity")
    public ResponseEntity<ApiResponse<List<LiquidityAlert>>> getHighSeverityAlerts(
            @RequestParam(defaultValue = "7") Integer minSeverity) {

        log.debug("Fetching high severity alerts (min severity: {})", minSeverity);

        List<LiquidityAlert> alerts = alertService.getHighSeverityAlerts(minSeverity);

        log.debug("Retrieved {} high severity alerts", alerts.size());

        return ResponseEntity.ok(ApiResponse.success(alerts));
    }

    @PostMapping("/{alertId}/acknowledge")
    public ResponseEntity<ApiResponse<LiquidityAlert>> acknowledgeAlert(
            @PathVariable Long alertId,
            @RequestParam String acknowledgedBy) {

        log.info("Acknowledging alert: {} by user: {}", alertId, acknowledgedBy);

        LiquidityAlert alert = alertService.acknowledgeAlert(alertId, acknowledgedBy);

        log.info("Alert {} acknowledged successfully", alertId);

        return ResponseEntity.ok(ApiResponse.success(alert, "Alert acknowledged successfully"));
    }

    @PostMapping("/{alertId}/resolve")
    public ResponseEntity<ApiResponse<LiquidityAlert>> resolveAlert(
            @PathVariable Long alertId,
            @RequestParam String resolutionNotes) {

        log.info("Resolving alert: {}", alertId);

        LiquidityAlert alert = alertService.resolveAlert(alertId, resolutionNotes);

        log.info("Alert {} resolved successfully", alertId);

        return ResponseEntity.ok(ApiResponse.success(alert, "Alert resolved successfully"));
    }

    @GetMapping("/stats/active-count")
    public ResponseEntity<ApiResponse<Long>> getActiveAlertCount() {
        log.debug("Fetching active alert count");

        Long count = alertService.getActiveAlertCount();

        log.debug("Active alert count: {}", count);

        return ResponseEntity.ok(ApiResponse.success(count));
    }
}