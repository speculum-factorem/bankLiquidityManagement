package com.bank.gateway.config;

import org.springframework.context.annotation.Configuration;

/**
 * Конфигурация API Gateway.
 * Маршруты определены в application.yml.
 * Этот класс может быть использован для дополнительной программной конфигурации при необходимости.
 */
@Configuration
public class GatewayConfig {
    // Маршруты настроены в application.yml
    // LoggingFilter является GlobalFilter и автоматически применяется ко всем маршрутам
}