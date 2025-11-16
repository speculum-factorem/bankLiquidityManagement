package com.bank.gateway.filter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.Mockito.*;

class AuthFilterTest {

    private AuthFilter authFilter;
    private GatewayFilterChain filterChain;

    @BeforeEach
    void setUp() {
        authFilter = new AuthFilter();
        filterChain = mock(GatewayFilterChain.class);
    }

    @Test
    void shouldRejectRequestWithoutAuthHeader() {
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/test")
                .build();

        ServerWebExchange exchange = MockServerWebExchange.from(request);
        GatewayFilter filter = authFilter.apply(new AuthFilter.Config());

        StepVerifier.create(filter.filter(exchange, filterChain))
                .verifyComplete();

        assert exchange.getResponse().getStatusCode() == HttpStatus.UNAUTHORIZED;
    }

    @Test
    void shouldAcceptRequestWithValidAuthHeader() {
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/test")
                .header(HttpHeaders.AUTHORIZATION, "Bearer valid-token")
                .build();

        ServerWebExchange exchange = MockServerWebExchange.from(request);
        GatewayFilter filter = authFilter.apply(new AuthFilter.Config());

        when(filterChain.filter(exchange)).thenReturn(Mono.empty());

        StepVerifier.create(filter.filter(exchange, filterChain))
                .verifyComplete();

        assert exchange.getResponse().getStatusCode() == null; // No error status
    }
}