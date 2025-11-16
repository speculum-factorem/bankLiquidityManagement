package com.bank.gateway.filter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.Mockito.*;

class LoggingFilterTest {

    private LoggingFilter loggingFilter;
    private GatewayFilterChain filterChain;

    @BeforeEach
    void setUp() {
        loggingFilter = new LoggingFilter();
        filterChain = mock(GatewayFilterChain.class);
        when(filterChain.filter(any())).thenReturn(Mono.empty());
    }

    @Test
    void shouldAddCorrelationIdWhenNotPresent() {
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/test")
                .build();

        ServerWebExchange exchange = MockServerWebExchange.from(request);

        StepVerifier.create(loggingFilter.filter(exchange, filterChain))
                .verifyComplete();

        String correlationId = exchange.getRequest().getHeaders().getFirst("correlationId");
        assert correlationId != null;
        assert correlationId.length() == 36; // UUID length
    }

    @Test
    void shouldUseExistingCorrelationId() {
        String existingCorrelationId = "test-correlation-id";

        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/test")
                .header("correlationId", existingCorrelationId)
                .build();

        ServerWebExchange exchange = MockServerWebExchange.from(request);

        StepVerifier.create(loggingFilter.filter(exchange, filterChain))
                .verifyComplete();

        String correlationId = exchange.getRequest().getHeaders().getFirst("correlationId");
        assert correlationId != null;
        assert correlationId.equals(existingCorrelationId);
    }
}