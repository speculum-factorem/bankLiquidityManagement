package com.bank.gateway.filter;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Bucket4j;
import io.github.bucket4j.Refill;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class RedisRateLimitGatewayFilterFactory extends AbstractGatewayFilterFactory<RedisRateLimitGatewayFilterFactory.Config> {

    // Резервное хранилище в памяти, если Redis недоступен
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    public RedisRateLimitGatewayFilterFactory(
            @Value("${spring.data.redis.host:}") String redisHost) {
        super(Config.class);
        // Пока используем хранилище в памяти. Интеграция с Redis может быть добавлена позже при необходимости
        log.info("Rate limiting initialized with in-memory storage (Redis support can be added)");
    }

    @Override
    public GatewayFilter apply(Config config) {
        int capacity = config.getCapacity() > 0 ? config.getCapacity() : 100;
        int refillTokens = config.getRefillTokens() > 0 ? config.getRefillTokens() : capacity;
        Duration refillDuration = config.getRefillDuration() != null ? 
            config.getRefillDuration() : Duration.ofMinutes(1);

        return (exchange, chain) -> {
            String clientIp = getClientIp(exchange.getRequest());
            Bucket bucket = buckets.computeIfAbsent(clientIp, k -> createNewBucket(capacity, refillTokens, refillDuration));

            if (bucket.tryConsume(1)) {
                log.debug("Rate limit check passed for client: {}", clientIp);
                return chain.filter(exchange);
            } else {
                log.warn("Rate limit exceeded for client: {} (capacity: {}, refill: {}/{})", 
                    clientIp, capacity, refillTokens, refillDuration);
                exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
                exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
                
                String errorBody = String.format(
                    "{\"error\": \"Rate limit exceeded\", \"message\": \"Too many requests. Limit: %d requests per %s\"}",
                    capacity, refillDuration
                );
                
                DataBuffer buffer = exchange.getResponse().bufferFactory()
                    .wrap(errorBody.getBytes());
                return exchange.getResponse().writeWith(Mono.just(buffer));
            }
        };
    }

    private Bucket createNewBucket(int capacity, int refillTokens, Duration refillDuration) {
        Refill refill = Refill.intervally(refillTokens, refillDuration);
        Bandwidth limit = Bandwidth.classic(capacity, refill);
        return Bucket4j.builder().addLimit(limit).build();
    }

    private String getClientIp(org.springframework.http.server.reactive.ServerHttpRequest request) {
        String xForwardedFor = request.getHeaders().getFirst("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        String xRealIp = request.getHeaders().getFirst("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        return request.getRemoteAddress() != null ?
            request.getRemoteAddress().getAddress().getHostAddress() : "unknown";
    }

    public static class Config {
        private int capacity = 100;
        private int refillTokens = 100;
        private Duration refillDuration = Duration.ofMinutes(1);

        public int getCapacity() {
            return capacity;
        }

        public void setCapacity(int capacity) {
            this.capacity = capacity;
        }

        public int getRefillTokens() {
            return refillTokens;
        }

        public void setRefillTokens(int refillTokens) {
            this.refillTokens = refillTokens;
        }

        public Duration getRefillDuration() {
            return refillDuration;
        }

        public void setRefillDuration(Duration refillDuration) {
            this.refillDuration = refillDuration;
        }
    }
}

