package com.bank.discovery.metrics;

import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.shared.Application;
import com.netflix.eureka.registry.PeerAwareInstanceRegistry;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class EurekaMetricsService {

    private final MeterRegistry meterRegistry;
    private final PeerAwareInstanceRegistry instanceRegistry;
    private final ConcurrentHashMap<String, Counter> counters = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Timer> timers = new ConcurrentHashMap<>();

    @Scheduled(fixedRate = 30000) // Every 30 seconds
    public void collectEurekaMetrics() {
        try {
            int totalInstances = 0;
            int upInstances = 0;
            int downInstances = 0;

            // Collect metrics from all applications
            for (Application application : instanceRegistry.getApplications().getRegisteredApplications()) {
                String appName = application.getName().toLowerCase();
                int appInstanceCount = application.getInstances().size();

                // Record application-specific metrics
                Gauge.builder("eureka.applications.instances", appInstanceCount, Number::intValue)
                        .tag("application", appName)
                        .register(meterRegistry);

                totalInstances += appInstanceCount;

                // Count UP and DOWN instances
                for (InstanceInfo instance : application.getInstances()) {
                    if (InstanceInfo.InstanceStatus.UP.equals(instance.getStatus())) {
                        upInstances++;
                    } else {
                        downInstances++;
                    }
                }

                log.debug("Collected metrics for application: {} - instances: {}", appName, appInstanceCount);
            }

            // Record overall metrics
            Gauge.builder("eureka.instances.total", totalInstances, Number::intValue)
                    .register(meterRegistry);

            Gauge.builder("eureka.instances.up", upInstances, Number::intValue)
                    .register(meterRegistry);

            Gauge.builder("eureka.instances.down", downInstances, Number::intValue)
                    .register(meterRegistry);

            Gauge.builder("eureka.applications.count",
                            instanceRegistry.getApplications().getRegisteredApplications().size(), Number::intValue)
                    .register(meterRegistry);

            log.debug("Eureka metrics collected - Total instances: {}, UP: {}, DOWN: {}",
                    totalInstances, upInstances, downInstances);

        } catch (Exception e) {
            log.error("Error collecting Eureka metrics", e);
        }
    }

    public void recordInstanceRegistration(String appName, String instanceId) {
        incrementCounter("eureka.registrations",
                "application", appName,
                "action", "register");

        log.info("Instance registered - App: {}, Instance: {}", appName, instanceId);
    }

    public void recordInstanceRenewal(String appName, String instanceId) {
        incrementCounter("eureka.renewals",
                "application", appName,
                "action", "renew");

        log.debug("Instance renewed - App: {}, Instance: {}", appName, instanceId);
    }

    public void recordInstanceCancellation(String appName, String instanceId) {
        incrementCounter("eureka.cancellations",
                "application", appName,
                "action", "cancel");

        log.info("Instance cancelled - App: {}, Instance: {}", appName, instanceId);
    }

    public void recordInstanceStatusChange(String appName, String instanceId, String oldStatus, String newStatus) {
        incrementCounter("eureka.status.changes",
                "application", appName,
                "old_status", oldStatus,
                "new_status", newStatus);

        log.info("Instance status changed - App: {}, Instance: {}, {} -> {}",
                appName, instanceId, oldStatus, newStatus);
    }

    public void recordPeerReplication(String peerNode, String action, boolean success, long duration) {
        String status = success ? "success" : "failure";

        incrementCounter("eureka.peer.replication",
                "peer", peerNode,
                "action", action,
                "status", status);

        recordTimer("eureka.peer.replication.duration", duration,
                "peer", peerNode,
                "action", action);

        if (!success) {
            log.warn("Peer replication failed - Peer: {}, Action: {}, Duration: {}ms",
                    peerNode, action, duration);
        } else {
            log.debug("Peer replication completed - Peer: {}, Action: {}, Duration: {}ms",
                    peerNode, action, duration);
        }
    }

    public void recordRegistryRead(long duration) {
        recordTimer("eureka.registry.read.duration", duration);
    }

    public void recordRegistryWrite(long duration) {
        recordTimer("eureka.registry.write.duration", duration);
    }

    private void incrementCounter(String name, String... tags) {
        String key = name + String.join("", tags);
        Counter counter = counters.computeIfAbsent(key,
                k -> Counter.builder(name)
                        .tags(tags)
                        .description("Eureka server metrics")
                        .register(meterRegistry));
        counter.increment();
    }

    private void recordTimer(String name, long duration, String... tags) {
        String key = name + String.join("", tags);
        Timer timer = timers.computeIfAbsent(key,
                k -> Timer.builder(name)
                        .tags(tags)
                        .description("Eureka server operation duration")
                        .register(meterRegistry));
        timer.record(duration, TimeUnit.MILLISECONDS);
    }
}