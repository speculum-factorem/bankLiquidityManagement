package com.bank.gateway.metrics;

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
public class GatewayMetrics {

    private final MeterRegistry meterRegistry;
    private final ConcurrentHashMap<String, Counter> requestCounters = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Counter> errorCounters = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Timer> requestTimers = new ConcurrentHashMap<>();

    public void recordRequest(String path, String method, int status, long duration) {
        String statusCategory = (status >= 200 && status < 300) ? "success" :
                (status >= 400 && status < 500) ? "client_error" : "server_error";

        // Метрики счетчиков
        incrementCounter("gateway.requests.total", "path", path, "method", method, "status", String.valueOf(status));
        incrementCounter("gateway.requests.by_status", "status_category", statusCategory);

        // Метрики времени
        recordTimer("gateway.request.duration", duration, "path", path, "method", method);

        // Метрики по HTTP методам
        incrementCounter("gateway.requests.by_method", "method", method);

        if (status >= 400) {
            incrementCounter("gateway.errors", "path", path, "method", method, "status", String.valueOf(status));
        }
    }

    public void recordRateLimit(String clientIp) {
        incrementCounter("gateway.rate_limits", "client_ip", clientIp);
    }

    public void recordAuthFailure(String path, String method) {
        incrementCounter("gateway.auth.failures", "path", path, "method", method);
    }

    private void incrementCounter(String name, String... tags) {
        String key = name + String.join("", tags);
        Counter counter = requestCounters.computeIfAbsent(key,
                k -> Counter.builder(name)
                        .tags(tags)
                        .register(meterRegistry));
        counter.increment();
    }

    private void recordTimer(String name, long duration, String... tags) {
        String key = name + String.join("", tags);
        Timer timer = requestTimers.computeIfAbsent(key,
                k -> Timer.builder(name)
                        .tags(tags)
                        .register(meterRegistry));
        timer.record(duration, TimeUnit.MILLISECONDS);
    }
}