package com.bank.gateway.filter;

import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class LoggingFilterTest {

    @Test
    void shouldAddCorrelationIdToRequest() {
        LoggingFilter filter = new LoggingFilter();
        GatewayFilterChain filterChain = mock(GatewayFilterChain.class);

        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/test")
                .build();

        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        when(filterChain.filter(any())).thenReturn(Mono.empty());

        StepVerifier.create(filter.filter(exchange, filterChain))
                .verifyComplete();

        String correlationId = exchange.getRequest().getHeaders().getFirst("correlationId");
        assertNotNull(correlationId);
        assertFalse(correlationId.isEmpty());
        
        verify(filterChain, times(1)).filter(any());
    }

    @Test
    void shouldUseExistingCorrelationId() {
        LoggingFilter filter = new LoggingFilter();
        GatewayFilterChain filterChain = mock(GatewayFilterChain.class);

        String existingCorrelationId = "existing-id-123";
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/test")
                .header("correlationId", existingCorrelationId)
                .build();

        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        when(filterChain.filter(any())).thenReturn(Mono.empty());

        StepVerifier.create(filter.filter(exchange, filterChain))
                .verifyComplete();

        String correlationId = exchange.getRequest().getHeaders().getFirst("correlationId");
        assertEquals(existingCorrelationId, correlationId);
        
        verify(filterChain, times(1)).filter(any());
    }
}
