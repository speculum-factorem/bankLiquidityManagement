package com.bank.discovery.health;

import com.netflix.discovery.shared.Application;
import com.netflix.discovery.shared.Applications;
import com.netflix.eureka.registry.PeerAwareInstanceRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DiscoveryHealthIndicatorTest {

    @Mock
    private PeerAwareInstanceRegistry instanceRegistry;

    private DiscoveryHealthIndicator healthIndicator;

    @BeforeEach
    void setUp() {
        healthIndicator = new DiscoveryHealthIndicator(instanceRegistry);
    }

    @Test
    void shouldReturnUpWhenApplicationsExist() {
        Applications applications = new Applications();
        Application app = new Application("TEST-SERVICE");
        applications.addApplication(app);

        when(instanceRegistry.getApplications()).thenReturn(applications);
        when(instanceRegistry.isSelfPreservationModeEnabled()).thenReturn(true);

        Health health = healthIndicator.health();

        assertEquals(Status.UP, health.getStatus());
        assertNotNull(health.getDetails().get("totalApplications"));
        assertNotNull(health.getDetails().get("totalInstances"));
    }

    @Test
    void shouldReturnUnknownWhenNoApplications() {
        Applications applications = new Applications();

        when(instanceRegistry.getApplications()).thenReturn(applications);

        Health health = healthIndicator.health();

        assertEquals(Status.UNKNOWN, health.getStatus());
    }

    @Test
    void shouldReturnDownWhenExceptionOccurs() {
        when(instanceRegistry.getApplications()).thenThrow(new RuntimeException("Database error"));

        Health health = healthIndicator.health();

        assertEquals(Status.DOWN, health.getStatus());
        assertNotNull(health.getDetails().get("error"));
    }
}