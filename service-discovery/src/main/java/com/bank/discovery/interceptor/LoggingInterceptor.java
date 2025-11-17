package com.bank.discovery.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.UUID;

@Slf4j
@Component
public class LoggingInterceptor implements HandlerInterceptor {

    private static final String CORRELATION_ID = "correlationId";
    private static final String START_TIME = "startTime";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String correlationId = getOrGenerateCorrelationId(request);

        MDC.put(CORRELATION_ID, correlationId);
        request.setAttribute(CORRELATION_ID, correlationId);
        request.setAttribute(START_TIME, System.currentTimeMillis());

        response.setHeader(CORRELATION_ID, correlationId);

        log.info("Eureka Request: {} {}, Remote: {}, User-Agent: {}",
                request.getMethod(),
                request.getRequestURI(),
                request.getRemoteAddr(),
                request.getHeader("User-Agent"));

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        Long startTime = (Long) request.getAttribute(START_TIME);
        if (startTime != null) {
            long duration = System.currentTimeMillis() - startTime;

            if (ex != null) {
                log.error("Eureka Request FAILED: {} {}, Status: {}, Duration: {}ms, Error: {}",
                        request.getMethod(),
                        request.getRequestURI(),
                        response.getStatus(),
                        duration,
                        ex.getMessage());
            } else {
                String logLevel = response.getStatus() >= 400 ? "WARN" : "DEBUG";
                if (logLevel.equals("WARN")) {
                    log.warn("Eureka Request completed: {} {}, Status: {}, Duration: {}ms",
                            request.getMethod(),
                            request.getRequestURI(),
                            response.getStatus(),
                            duration);
                } else {
                    log.debug("Eureka Request completed: {} {}, Status: {}, Duration: {}ms",
                            request.getMethod(),
                            request.getRequestURI(),
                            response.getStatus(),
                            duration);
                }
            }
        }

        MDC.clear();
    }

    private String getOrGenerateCorrelationId(HttpServletRequest request) {
        String correlationId = request.getHeader(CORRELATION_ID);
        return correlationId != null ? correlationId : UUID.randomUUID().toString();
    }
}