package com.bank.config.filter;

import com.bank.config.metrics.ConfigServerMetrics;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class MetricsFilter extends OncePerRequestFilter {

    private final ConfigServerMetrics configServerMetrics;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        long startTime = System.currentTimeMillis();
        String path = request.getRequestURI();

        try {
            filterChain.doFilter(request, response);
        } finally {
            long duration = System.currentTimeMillis() - startTime;

            // Парсим путь для извлечения информации о конфигурации
            if (path.contains("/")) {
                String[] pathParts = path.split("/");
                if (pathParts.length >= 4) {
                    String application = pathParts[pathParts.length - 3];
                    String profile = pathParts[pathParts.length - 2];
                    String label = pathParts[pathParts.length - 1];

                    boolean found = response.getStatus() == 200;

                    configServerMetrics.recordConfigRequest(application, profile, label, found, duration);

                    log.debug("Config request - App: {}, Profile: {}, Label: {}, Found: {}, Duration: {}ms",
                            application, profile, label, found, duration);
                }
            }
        }
    }
}