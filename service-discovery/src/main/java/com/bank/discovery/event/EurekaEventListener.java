package com.bank.discovery.event;

import com.bank.discovery.metrics.EurekaMetricsService;
import com.netflix.appinfo.InstanceInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.netflix.eureka.server.event.*;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class EurekaEventListener {

    private final EurekaMetricsService metricsService;

    @EventListener
    public void handleEurekaInstanceRegisteredEvent(EurekaInstanceRegisteredEvent event) {
        InstanceInfo instanceInfo = event.getInstanceInfo();
        String appName = instanceInfo.getAppName();
        String instanceId = instanceInfo.getInstanceId();

        metricsService.recordInstanceRegistration(appName, instanceId);

        log.info("Instance REGISTERED - App: {}, Instance: {}, IP: {}, Port: {}",
                appName, instanceId, instanceInfo.getIPAddr(), instanceInfo.getPort());
    }

    @EventListener
    public void handleEurekaInstanceRenewedEvent(EurekaInstanceRenewedEvent event) {
        String appName = event.getAppName();
        String instanceId = event.getInstanceId();

        metricsService.recordInstanceRenewal(appName, instanceId);

        log.debug("Instance RENEWED - App: {}, Instance: {}", appName, instanceId);
    }

    @EventListener
    public void handleEurekaInstanceCanceledEvent(EurekaInstanceCanceledEvent event) {
        String appName = event.getAppName();
        String instanceId = event.getServerId();

        metricsService.recordInstanceCancellation(appName, instanceId);

        log.info("Instance CANCELLED - App: {}, Instance: {}", appName, instanceId);
    }

    @EventListener
    public void handleEurekaRegistryAvailableEvent(EurekaRegistryAvailableEvent event) {
        log.info("Eureka Registry is now AVAILABLE");

        // Record registry availability metric
        metricsService.incrementCounter("eureka.registry.available");
    }

    @EventListener
    public void handleEurekaServerStartedEvent(EurekaServerStartedEvent event) {
        log.info("Eureka Server has STARTED");

        // Record server start metric
        metricsService.incrementCounter("eureka.server.started");
    }

    @EventListener
    public void handleEurekaInstanceStatusChangedEvent(EurekaInstanceStatusChangedEvent event) {
        String appName = event.getAppName();
        String instanceId = event.getInstanceId();
        String oldStatus = event.getStatus().toString();
        String newStatus = event.getInstanceInfo().getStatus().toString();

        metricsService.recordInstanceStatusChange(appName, instanceId, oldStatus, newStatus);

        log.info("Instance STATUS CHANGED - App: {}, Instance: {}, Status: {} -> {}",
                appName, instanceId, oldStatus, newStatus);
    }
}