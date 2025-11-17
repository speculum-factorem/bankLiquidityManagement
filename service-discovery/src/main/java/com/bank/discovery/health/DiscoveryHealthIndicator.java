package com.bank.discovery.health;

import com.netflix.eureka.registry.PeerAwareInstanceRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DiscoveryHealthIndicator implements HealthIndicator {

    private final PeerAwareInstanceRegistry instanceRegistry;

    @Override
    public Health health() {
        try {
            int totalApplications = instanceRegistry.getApplications().getRegisteredApplications().size();
            int totalInstances = instanceRegistry.getApplications().getRegisteredApplications()
                    .stream()
                    .mapToInt(app -> app.getInstances().size())
                    .sum();

            Health.Builder status = totalApplications > 0 ? Health.up() : Health.unknown();

            return status
                    .withDetail("totalApplications", totalApplications)
                    .withDetail("totalInstances", totalInstances)
                    .withDetail("registryStatus", "ACTIVE")
                    .withDetail("selfPreservationMode", instanceRegistry.isSelfPreservationModeEnabled())
                    .build();

        } catch (Exception e) {
            log.error("Discovery health check failed", e);
            return Health.down()
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }
}