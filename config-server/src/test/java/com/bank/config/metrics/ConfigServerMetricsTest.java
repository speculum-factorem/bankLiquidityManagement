package com.bank.config.metrics;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ConfigServerMetricsTest {

    private ConfigServerMetrics configServerMetrics;
    private MeterRegistry meterRegistry;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        configServerMetrics = new ConfigServerMetrics(meterRegistry);
    }

    @Test
    void shouldRecordConfigRequestMetrics() {
        configServerMetrics.recordConfigRequest("liquidity-service", "default", "main", true, 150L);

        double totalRequests = meterRegistry.get("config.server.requests.total")
                .tag("application", "liquidity-service")
                .tag("profile", "default")
                .tag("label", "main")
                .tag("status", "found")
                .counter()
                .count();

        assertEquals(1.0, totalRequests);
    }

    @Test
    void shouldRecordConfigRefreshMetrics() {
        configServerMetrics.recordConfigRefresh("transaction-service", "prod");

        double refreshes = meterRegistry.get("config.server.refreshes")
                .tag("application", "transaction-service")
                .tag("profile", "prod")
                .counter()
                .count();

        assertEquals(1.0, refreshes);
    }

    @Test
    void shouldRecordGitOperationMetrics() {
        configServerMetrics.recordGitOperation("clone", true, 2000L);

        double gitOperations = meterRegistry.get("config.server.git.operations")
                .tag("operation", "clone")
                .tag("status", "success")
                .counter()
                .count();

        assertEquals(1.0, gitOperations);
    }
}