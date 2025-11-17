package com.bank.discovery.metrics;

import com.netflix.discovery.shared.Application;
import com.netflix.discovery.shared.Applications;
import com.netflix.eureka.registry.PeerAwareInstanceRegistry;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EurekaMetricsServiceTest {

    @Mock
    private PeerAwareInstanceRegistry instanceRegistry;

    private EurekaMetricsService eurekaMetricsService;
    private MeterRegistry meterRegistry;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        eurekaMetricsService = new EurekaMetricsService(meterRegistry, instanceRegistry);
    }

    @Test
    void shouldRecordInstanceRegistration() {
        eurekaMetricsService.recordInstanceRegistration("liquidity-service", "instance-1");

        double registrations = meterRegistry.get("eureka.registrations")
                .tag("application", "liquidity-service")
                .tag("action", "register")
                .counter()
                .count();

        assertEquals(1.0, registrations);
    }

    @Test
    void shouldRecordInstanceRenewal() {
        eurekaMetricsService.recordInstanceRenewal("risk-service", "instance-2");

        double renewals = meterRegistry.get("eureka.renewals")
                .tag("application", "risk-service")
                .tag("action", "renew")
                .counter()
                .count();

        assertEquals(1.0, renewals);
    }

    @Test
    void shouldRecordInstanceCancellation() {
        eurekaMetricsService.recordInstanceCancellation("transaction-service", "instance-3");

        double cancellations = meterRegistry.get("eureka.cancellations")
                .tag("application", "transaction-service")
                .tag("action", "cancel")
                .counter()
                .count();

        assertEquals(1.0, cancellations);
    }

    @Test
    void shouldRecordInstanceStatusChange() {
        eurekaMetricsService.recordInstanceStatusChange("api-gateway", "instance-4", "UP", "DOWN");

        double statusChanges = meterRegistry.get("eureka.status.changes")
                .tag("application", "api-gateway")
                .tag("old_status", "UP")
                .tag("new_status", "DOWN")
                .counter()
                .count();

        assertEquals(1.0, statusChanges);
    }

    @Test
    void shouldCollectEurekaMetrics() {
        Applications applications = new Applications();
        Application app = new Application("TEST-SERVICE");
        applications.addApplication(app);

        when(instanceRegistry.getApplications()).thenReturn(applications);

        eurekaMetricsService.collectEurekaMetrics();

        // Verify that metrics collection was attempted
        verify(instanceRegistry).getApplications();
    }
}