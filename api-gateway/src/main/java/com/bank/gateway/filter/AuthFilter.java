package com.bank.gateway.filter;

import com.bank.gateway.security.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class AuthGatewayFilterFactory extends AbstractGatewayFilterFactory<AuthGatewayFilterFactory.Config> {

    private final JwtUtil jwtUtil;

    public AuthGatewayFilterFactory(JwtUtil jwtUtil) {
        super(Config.class);
        this.jwtUtil = jwtUtil;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            String authHeader = request.getHeaders().getFirst("Authorization");

            // Проверка наличия и корректности заголовка Authorization
            if (!StringUtils.hasText(authHeader) || !authHeader.startsWith("Bearer ")) {
                log.warn("Missing or invalid Authorization header for request: {}", request.getPath());
                return buildErrorResponse(exchange, HttpStatus.UNAUTHORIZED, 
                    "Missing or invalid Authorization header");
            }

            // Извлечение токена из заголовка
            String token = authHeader.substring(7);
            
            // Валидация JWT токена
            if (!jwtUtil.validateToken(token)) {
                log.warn("Invalid or expired JWT token for request: {}", request.getPath());
                return buildErrorResponse(exchange, HttpStatus.UNAUTHORIZED, 
                    "Invalid or expired JWT token");
            }

            // Извлечение имени пользователя из токена и добавление в заголовки для последующих сервисов
            try {
                String username = jwtUtil.extractUsername(token);
                ServerHttpRequest modifiedRequest = request.mutate()
                    .header("X-User-Name", username)
                    .header("X-Authenticated", "true")
                    .build();
                
                log.debug("JWT token validated successfully for user: {}", username);
                return chain.filter(exchange.mutate().request(modifiedRequest).build());
            } catch (Exception e) {
                log.error("Error extracting user information from token", e);
                return buildErrorResponse(exchange, HttpStatus.UNAUTHORIZED, 
                    "Error processing authentication token");
            }
        };
    }

    private Mono<Void> buildErrorResponse(org.springframework.web.server.ServerWebExchange exchange, 
                                          HttpStatus status, String message) {
        exchange.getResponse().setStatusCode(status);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
        
        String errorBody = String.format(
            "{\"error\": \"%s\", \"message\": \"%s\", \"status\": %d}", 
            status.getReasonPhrase(), message, status.value()
        );
        
        DataBuffer buffer = exchange.getResponse().bufferFactory()
            .wrap(errorBody.getBytes());
        return exchange.getResponse().writeWith(Mono.just(buffer));
    }

    // Класс конфигурации фильтра аутентификации
    public static class Config {
        // Здесь можно добавить параметры конфигурации при необходимости
        // Например: allowedRoles, skipPaths и т.д.
    }
}