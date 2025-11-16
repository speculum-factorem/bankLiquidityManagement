package com.bank.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.server.WebFilter;

@Configuration
public class LoggingConfig {

    @Bean
    public WebFilter correlationIdFilter() {
        return (exchange, chain) -> {
            // MDC уже настроен в LoggingFilter
            return chain.filter(exchange);
        };
    }
}