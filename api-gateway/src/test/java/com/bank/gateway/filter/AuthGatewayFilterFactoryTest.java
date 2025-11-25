package com.bank.gateway.filter;

import com.bank.gateway.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthGatewayFilterFactoryTest {

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private GatewayFilterChain filterChain;

    private AuthGatewayFilterFactory authFilter;

    @BeforeEach
    void setUp() {
        authFilter = new AuthGatewayFilterFactory(jwtUtil);
    }

    @Test
    void shouldRejectRequestWithoutAuthHeader() {
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/test")
                .build();

        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        GatewayFilter filter = authFilter.apply(new AuthGatewayFilterFactory.Config());

        StepVerifier.create(filter.filter(exchange, filterChain))
                .verifyComplete();

        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
        verify(filterChain, never()).filter(any());
    }

    @Test
    void shouldRejectRequestWithInvalidAuthHeader() {
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/test")
                .header(HttpHeaders.AUTHORIZATION, "Invalid token")
                .build();

        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        GatewayFilter filter = authFilter.apply(new AuthGatewayFilterFactory.Config());

        StepVerifier.create(filter.filter(exchange, filterChain))
                .verifyComplete();

        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
        verify(filterChain, never()).filter(any());
    }

    @Test
    void shouldRejectRequestWithInvalidToken() {
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/test")
                .header(HttpHeaders.AUTHORIZATION, "Bearer invalid-token")
                .build();

        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        GatewayFilter filter = authFilter.apply(new AuthGatewayFilterFactory.Config());

        when(jwtUtil.validateToken("invalid-token")).thenReturn(false);

        StepVerifier.create(filter.filter(exchange, filterChain))
                .verifyComplete();

        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
        verify(filterChain, never()).filter(any());
    }

    @Test
    void shouldAcceptRequestWithValidToken() {
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/test")
                .header(HttpHeaders.AUTHORIZATION, "Bearer valid-token")
                .build();

        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        GatewayFilter filter = authFilter.apply(new AuthGatewayFilterFactory.Config());

        when(jwtUtil.validateToken("valid-token")).thenReturn(true);
        when(jwtUtil.extractUsername("valid-token")).thenReturn("testuser");
        when(filterChain.filter(exchange)).thenReturn(Mono.empty());

        StepVerifier.create(filter.filter(exchange, filterChain))
                .verifyComplete();

        assertEquals("testuser", exchange.getRequest().getHeaders().getFirst("X-User-Name"));
        assertEquals("true", exchange.getRequest().getHeaders().getFirst("X-Authenticated"));
        verify(filterChain, times(1)).filter(any());
    }
}
