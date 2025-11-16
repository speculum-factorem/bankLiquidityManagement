package com.bank.config.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class ConfigServerMetrics {

    private final MeterRegistry meterRegistry;
    private final ConcurrentHashMap<String, Counter> configCounters = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Timer> configTimers = new ConcurrentHashMap<>();

    public void recordConfigRequest(String application, String profile, String label, boolean found, long duration) {
        String status = found ? "found" : "not_found";

        // Метрики счетчиков
        incrementCounter("config.server.requests.total",
                "application", application,
                "profile", profile,
                "label", label,
                "status", status);

        // Метрики времени
        recordTimer("config.server.request.duration", duration,
                "application", application,
                "profile", profile);

        // Метрики по профилям
        incrementCounter("config.server.requests.by_profile", "profile", profile);

        // Метрики по приложениям
        incrementCounter("config.server.requests.by_application", "application", application);

        if (!found) {
            incrementCounter("config.server.misses",
                    "application", application,
                    "profile", profile);
        }
    }

    public void recordConfigRefresh(String application, String profile) {
        incrementCounter("config.server.refreshes",
                "application", application,
                "profile", profile);
    }

    public void recordGitOperation(String operation, boolean success, long duration) {
        String status = success ? "success" : "failure";

        incrementCounter("config.server.git.operations",
                "operation", operation,
                "status", status);

        recordTimer("config.server.git.operation.duration", duration,
                "operation", operation);
    }

    public void recordPropertyResolution(String application, String property, boolean resolved) {
        String status = resolved ? "resolved" : "unresolved";

        incrementCounter("config.server.property.resolutions",
                "application", application,
                "property", property,
                "status", status);
    }

    private void incrementCounter(String name, String... tags) {
        String key = name + String.join("", tags);
        Counter counter = configCounters.computeIfAbsent(key,
                k -> Counter.builder(name)
                        .tags(tags)
                        .description("Config Server metrics")
                        .register(meterRegistry));
        counter.increment();
    }

    private void recordTimer(String name, long duration, String... tags) {
        String key = name + String.join("", tags);
        Timer timer = configTimers.computeIfAbsent(key,
                k -> Timer.builder(name)
                        .tags(tags)
                        .description("Config Server operation duration")
                        .register(meterRegistry));
        timer.record(duration, TimeUnit.MILLISECONDS);
    }
}