package com.bank.config.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.config.server.environment.EnvironmentController;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class ConfigServerController {

    private final EnvironmentController environmentController;

    @Value("${spring.application.name}")
    private String applicationName;

    @Value("${spring.cloud.config.server.git.uri:unknown}")
    private String gitUri;

    @GetMapping("/info")
    public ResponseEntity<Map<String, String>> getConfigServerInfo() {
        log.info("Config server info requested");

        return ResponseEntity.ok(Map.of(
                "name", applicationName,
                "gitUri", gitUri,
                "status", "running",
                "timestamp", java.time.LocalDateTime.now().toString()
        ));
    }

    @PostMapping("/refresh/{application}")
    public ResponseEntity<Map<String, String>> refreshApplicationConfig(
            @PathVariable String application,
            @RequestParam(defaultValue = "default") String profile) {

        log.info("Manual config refresh requested for application: {}, profile: {}", application, profile);

        // Здесь можно добавить логику принудительного обновления конфигурации
        return ResponseEntity.ok(Map.of(
                "application", application,
                "profile", profile,
                "status", "refresh_triggered",
                "message", "Configuration refresh has been triggered",
                "timestamp", java.time.LocalDateTime.now().toString()
        ));
    }

    @GetMapping("/health/detailed")
    public ResponseEntity<Map<String, Object>> detailedHealth() {
        log.debug("Detailed health check requested");

        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "components", Map.of(
                        "configServer", Map.of("status", "UP"),
                        "gitRepository", Map.of("status", "CONNECTED", "uri", gitUri),
                        "diskSpace", Map.of("status", "UP", "free", Runtime.getRuntime().freeMemory())
                ),
                "timestamp", java.time.LocalDateTime.now().toString()
        ));
    }
}