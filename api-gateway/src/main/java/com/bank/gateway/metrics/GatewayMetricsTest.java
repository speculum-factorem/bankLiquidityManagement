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

        double totalRequests = meterRegistry.get("gateway.requests.total")
                .tag("path", "/api/test")
                .tag("method", "GET")
                .tag("status", "200")
                .counter()
                .count();

        assertEquals(1.0, totalRequests);
    }

    @Test
    void shouldRecordRateLimitMetrics() {
        gatewayMetrics.recordRateLimit("192.168.1.1");

        double rateLimits = meterRegistry.get("gateway.rate_limits")
                .tag("client_ip", "192.168.1.1")
                .counter()
                .count();

        assertEquals(1.0, rateLimits);
    }
}