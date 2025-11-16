package com.bank.config.health;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.cloud.config.server.environment.JGitEnvironmentRepository;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class GitRepositoryHealthIndicator implements HealthIndicator {

    private final JGitEnvironmentRepository gitRepository;

    @Override
    public Health health() {
        try {
            // Проверяем доступность Git репозитория
            gitRepository.findOne("config-server", "default", "main");

            return Health.up()
                    .withDetail("repository", gitRepository.getUri())
                    .withDetail("basedir", gitRepository.getBasedir())
                    .withDetail("defaultLabel", gitRepository.getDefaultLabel())
                    .build();

        } catch (Exception e) {
            log.error("Git repository health check failed", e);
            return Health.down()
                    .withDetail("repository", gitRepository.getUri())
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }
}