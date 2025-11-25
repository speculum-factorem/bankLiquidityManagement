package com.bank.gateway.filter;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

// Фильтр для реализации паттерна Circuit Breaker в API Gateway
@Slf4j
@Component
public class CircuitBreakerGatewayFilterFactory extends AbstractGatewayFilterFactory<CircuitBreakerGatewayFilterFactory.Config> {

    private final CircuitBreakerRegistry circuitBreakerRegistry;

    public CircuitBreakerGatewayFilterFactory(CircuitBreakerRegistry circuitBreakerRegistry) {
        super(Config.class);
        this.circuitBreakerRegistry = circuitBreakerRegistry;
    }

    // Применение Circuit Breaker к запросу
    @Override
    public GatewayFilter apply(Config config) {
        // Получение имени circuit breaker из конфигурации или использование значения по умолчанию
        String circuitBreakerName = config.getName() != null ? config.getName() : "default";
        CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker(circuitBreakerName);

        return (exchange, chain) -> {
            log.debug("Applying circuit breaker '{}' to request: {}", circuitBreakerName, exchange.getRequest().getPath());

            // Применение Circuit Breaker к цепочке фильтров
            return chain.filter(exchange)
                .transformDeferred(CircuitBreakerOperator.of(circuitBreaker))
                .onErrorResume(throwable -> {
                    log.error("Circuit breaker '{}' error for request: {}", circuitBreakerName, exchange.getRequest().getPath(), throwable);
                    
                    // Обработка ошибок в зависимости от состояния circuit breaker
                    HttpStatus status = HttpStatus.SERVICE_UNAVAILABLE;
                    String message = "Service temporarily unavailable. Please try again later.";
                    
                    // Определение сообщения в зависимости от состояния circuit breaker
                    if (circuitBreaker.getState().toString().equals("OPEN")) {
                        message = "Circuit breaker is OPEN. Service is unavailable.";
                    } else if (circuitBreaker.getState().toString().equals("HALF_OPEN")) {
                        message = "Circuit breaker is HALF_OPEN. Service is testing recovery.";
                    }
                    
                    exchange.getResponse().setStatusCode(status);
                    exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
                    
                    String errorBody = String.format(
                        "{\"error\": \"%s\", \"message\": \"%s\", \"circuitBreakerState\": \"%s\"}",
                        status.getReasonPhrase(), message, circuitBreaker.getState()
                    );
                    
                    DataBuffer buffer = exchange.getResponse().bufferFactory()
                        .wrap(errorBody.getBytes());
                    return exchange.getResponse().writeWith(Mono.just(buffer));
                });
        };
    }

    // Класс конфигурации фильтра Circuit Breaker
    public static class Config {
        // Имя circuit breaker для использования
        private String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}

