package com.bank.config.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.config.server.environment.EnvironmentRepository;
import org.springframework.cloud.config.server.environment.JGitEnvironmentRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.ConfigurableEnvironment;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class ConfigServerConfig {

    private final ConfigurableEnvironment environment;

    @Bean
    public EnvironmentRepository environmentRepository() {
        JGitEnvironmentRepository repository = new JGitEnvironmentRepository();
        // Дополнительная конфигурация репозитория Git
        log.info("Configuring Git environment repository");
        return repository;
    }
}