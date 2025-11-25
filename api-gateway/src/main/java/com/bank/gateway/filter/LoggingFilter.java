package com.bank.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

// Глобальный фильтр для логирования запросов с использованием MDC
@Slf4j
@Component
public class LoggingFilter implements GlobalFilter, Ordered {

    // Ключи для MDC и атрибутов запроса
    private static final String CORRELATION_ID = "correlationId";
    private static final String START_TIME = "startTime";

    // Фильтр для логирования всех входящих запросов с correlation ID
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // Получение или генерация correlation ID для трассировки запроса
        String correlationId = getOrGenerateCorrelationId(exchange.getRequest());

        // Добавление correlation ID в MDC для логирования
        MDC.put(CORRELATION_ID, correlationId);
        exchange.getAttributes().put(CORRELATION_ID, correlationId);
        exchange.getAttributes().put(START_TIME, System.currentTimeMillis());

        ServerHttpRequest request = exchange.getRequest()
                .mutate()
                .header(CORRELATION_ID, correlationId)
                .build();

        log.info("Incoming request: {} {}, Headers: {}, QueryParams: {}",
                request.getMethod(),
                request.getPath(),
                request.getHeaders(),
                request.getQueryParams());

        // Продолжение цепочки фильтров с логированием результата
        return chain.filter(exchange.mutate().request(request).build())
                .doOnSuccessOrError((result, throwable) -> {
                    // Расчет времени выполнения запроса
                    long startTime = exchange.getAttribute(START_TIME);
                    long duration = System.currentTimeMillis() - startTime;

                    // Логирование результата запроса (успех или ошибка)
                    if (throwable != null) {
                        log.error("Request failed: {} {}, Duration: {}ms, Error: {}",
                                request.getMethod(),
                                request.getPath(),
                                duration,
                                throwable.getMessage());
                    } else {
                        log.info("Request completed: {} {}, Status: {}, Duration: {}ms",
                                request.getMethod(),
                                request.getPath(),
                                exchange.getResponse().getStatusCode(),
                                duration);
                    }
                    // Очистка MDC после завершения запроса
                    MDC.clear();
                });
    }

    // Получение correlation ID из заголовка или генерация нового
    private String getOrGenerateCorrelationId(ServerHttpRequest request) {
        String correlationId = request.getHeaders().getFirst(CORRELATION_ID);
        return correlationId != null ? correlationId : UUID.randomUUID().toString();
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}