package com.bank.discovery.controller;

import com.bank.discovery.dto.ApiResponse;
import com.netflix.discovery.shared.Application;
import com.netflix.eureka.registry.PeerAwareInstanceRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/admin/discovery")
@RequiredArgsConstructor
public class DiscoveryAdminController {

    private final PeerAwareInstanceRegistry instanceRegistry;

    @GetMapping("/applications")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getRegisteredApplications() {
        log.debug("Fetching all registered applications");

        List<Map<String, Object>> applications = instanceRegistry.getApplications()
                .getRegisteredApplications()
                .stream()
                .map(this::mapApplicationToResponse)
                .collect(Collectors.toList());

        log.debug("Retrieved {} registered applications", applications.size());

        return ResponseEntity.ok(ApiResponse.success(applications));
    }

    @GetMapping("/applications/{appName}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getApplication(@PathVariable String appName) {
        log.debug("Fetching application: {}", appName);

        Application application = instanceRegistry.getApplication(appName.toUpperCase());

        if (application == null) {
            log.warn("Application not found: {}", appName);
            return ResponseEntity.ok(ApiResponse.error("Application not found: " + appName));
        }

        Map<String, Object> response = mapApplicationToResponse(application);

        log.debug("Retrieved application: {} with {} instances", appName, application.getInstances().size());

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDiscoveryStats() {
        log.debug("Fetching discovery statistics");

        Map<String, Object> stats = new HashMap<>();

        int totalApplications = instanceRegistry.getApplications().getRegisteredApplications().size();
        int totalInstances = instanceRegistry.getApplications().getRegisteredApplications()
                .stream()
                .mapToInt(app -> app.getInstances().size())
                .sum();

        stats.put("totalApplications", totalApplications);
        stats.put("totalInstances", totalInstances);
        stats.put("timestamp", java.time.LocalDateTime.now().toString());

        // Add application-specific counts
        Map<String, Integer> appInstanceCounts = new HashMap<>();
        instanceRegistry.getApplications().getRegisteredApplications()
                .forEach(app -> appInstanceCounts.put(app.getName(), app.getInstances().size()));

        stats.put("applicationInstanceCounts", appInstanceCounts);

        log.debug("Discovery statistics - Applications: {}, Instances: {}", totalApplications, totalInstances);

        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    @PostMapping("/applications/{appName}/instances/{instanceId}/status")
    public ResponseEntity<ApiResponse<String>> updateInstanceStatus(
            @PathVariable String appName,
            @PathVariable String instanceId,
            @RequestParam String status) {

        log.info("Updating instance status - App: {}, Instance: {}, Status: {}", appName, instanceId, status);

        // Note: In a real implementation, you would call Eureka's internal methods
        // to update instance status. This is a simplified example.

        log.warn("Instance status update not implemented in this example - App: {}, Instance: {}, Status: {}",
                appName, instanceId, status);

        return ResponseEntity.ok(ApiResponse.success(
                String.format("Status update requested for %s/%s to %s", appName, instanceId, status)));
    }

    private Map<String, Object> mapApplicationToResponse(Application application) {
        Map<String, Object> appMap = new HashMap<>();
        appMap.put("name", application.getName());
        appMap.put("instanceCount", application.getInstances().size());

        List<Map<String, Object>> instances = application.getInstances()
                .stream()
                .map(instance -> {
                    Map<String, Object> instanceMap = new HashMap<>();
                    instanceMap.put("instanceId", instance.getInstanceId());
                    instanceMap.put("hostName", instance.getHostName());
                    instanceMap.put("ipAddress", instance.getIPAddr());
                    instanceMap.put("port", instance.getPort());
                    instanceMap.put("status", instance.getStatus().name());
                    instanceMap.put("homePageUrl", instance.getHomePageUrl());
                    instanceMap.put("healthCheckUrl", instance.getHealthCheckUrl());
                    instanceMap.put("lastUpdatedTimestamp", instance.getLastUpdatedTimestamp());
                    instanceMap.put("lastDirtyTimestamp", instance.getLastDirtyTimestamp());
                    return instanceMap;
                })
                .collect(Collectors.toList());

        appMap.put("instances", instances);

        return appMap;
    }
}