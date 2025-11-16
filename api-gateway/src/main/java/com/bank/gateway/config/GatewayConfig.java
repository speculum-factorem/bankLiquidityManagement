package com.bank.gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("liquidity-service", r -> r
                        .path("/api/liquidity/**")
                        .filters(f -> f
                                .stripPrefix(1)
                                .filter(new LoggingFilter())
                                .addRequestHeader("X-API-Version", "1.0"))
                        .uri("lb://liquidity-service"))
                .route("transaction-service", r -> r
                        .path("/api/transactions/**")
                        .filters(f -> f
                                .stripPrefix(1)
                                .filter(new LoggingFilter())
                                .addRequestHeader("X-API-Version", "1.0"))
                        .uri("lb://transaction-service"))
                .route("risk-service", r -> r
                        .path("/api/risk/**")
                        .filters(f -> f
                                .stripPrefix(1)
                                .filter(new LoggingFilter())
                                .addRequestHeader("X-API-Version", "1.0"))
                        .uri("lb://risk-service"))
                .build();
    }
}