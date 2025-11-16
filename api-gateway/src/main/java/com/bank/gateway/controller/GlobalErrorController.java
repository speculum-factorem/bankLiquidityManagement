package com.bank.gateway.controller;

import com.bank.gateway.dto.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.HttpMessageReader;
import org.springframework.http.codec.HttpMessageWriter;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.reactive.result.view.ViewResolver;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;

@Slf4j
@Component
@Order(-2)
public class GlobalErrorController implements ErrorWebExceptionHandler {

    private List<HttpMessageReader<?>> messageReaders = Collections.emptyList();
    private List<HttpMessageWriter<?>> messageWriters = Collections.emptyList();
    private List<ViewResolver> viewResolvers = Collections.emptyList();

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        ServerHttpRequest request = exchange.getRequest();
        log.error("Global error handling for request: {} {}, Error: {}",
                request.getMethod(), request.getPath(), ex.getMessage(), ex);

        ServerRequest serverRequest = ServerRequest.create(exchange, this.messageReaders);

        return RouterFunctions.route(RequestPredicates.all(), this::renderErrorResponse)
                .route(serverRequest)
                .switchIfEmpty(Mono.error(ex))
                .flatMap(handler -> handler.handle(serverRequest))
                .flatMap(response -> write(exchange, response));
    }

    private Mono<ServerResponse> renderErrorResponse(ServerRequest request) {
        ServerHttpRequest httpRequest = request.exchange().getRequest();

        ApiResponse<Object> errorResponse = ApiResponse.builder()
                .success(false)
                .message("Internal server error")
                .path(httpRequest.getPath().value())
                .timestamp(java.time.LocalDateTime.now())
                .build();

        return ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(errorResponse));
    }

    private Mono<? extends Void> write(ServerWebExchange exchange, ServerResponse response) {
        exchange.getResponse().getHeaders().setContentType(response.headers().getContentType());
        return response.writeTo(exchange, new ResponseContext());
    }

    public void setMessageReaders(List<HttpMessageReader<?>> messageReaders) {
        this.messageReaders = messageReaders;
    }

    public void setMessageWriters(List<HttpMessageWriter<?>> messageWriters) {
        this.messageWriters = messageWriters;
    }

    public void setViewResolvers(List<ViewResolver> viewResolvers) {
        this.viewResolvers = viewResolvers;
    }

    private class ResponseContext implements ServerResponse.Context {
        @Override
        public List<HttpMessageWriter<?>> messageWriters() {
            return GlobalErrorController.this.messageWriters;
        }

        @Override
        public List<ViewResolver> viewResolvers() {
            return GlobalErrorController.this.viewResolvers;
        }
    }
}