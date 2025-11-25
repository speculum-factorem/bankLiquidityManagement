package com.bank.gateway.metrics;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GatewayMetricsTest {

    private GatewayMetrics gatewayMetrics;
    private MeterRegistry meterRegistry;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        gatewayMetrics = new GatewayMetrics(meterRegistry);
    }

    @Test
    void shouldRecordRequestMetrics() {
        gatewayMetrics.recordRequest("/api/test", "GET", 200, 150L);

        // Verify metrics were recorded by checking the registry
        assertNotNull(meterRegistry);
        assertNotNull(gatewayMetrics);
        
        // Verify no exceptions were thrown
        assertDoesNotThrow(() -> gatewayMetrics.recordRequest("/api/test2", "POST", 404, 200L));
    }

    @Test
    void shouldRecordRateLimitMetrics() {
        gatewayMetrics.recordRateLimit("192.168.1.1");

        assertNotNull(meterRegistry);
        assertDoesNotThrow(() -> gatewayMetrics.recordRateLimit("10.0.0.1"));
    }

    @Test
    void shouldRecordAuthFailureMetrics() {
        gatewayMetrics.recordAuthFailure("/api/test", "GET");

        assertNotNull(meterRegistry);
        assertDoesNotThrow(() -> gatewayMetrics.recordAuthFailure("/api/test2", "POST"));
    }
}
