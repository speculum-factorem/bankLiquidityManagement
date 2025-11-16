package com.bank.config.health;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;
import org.springframework.cloud.config.server.environment.JGitEnvironmentRepository;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GitRepositoryHealthIndicatorTest {

    private GitRepositoryHealthIndicator healthIndicator;
    private JGitEnvironmentRepository gitRepository;

    @BeforeEach
    void setUp() {
        gitRepository = mock(JGitEnvironmentRepository.class);
        healthIndicator = new GitRepositoryHealthIndicator(gitRepository);
    }

    @Test
    void shouldReturnUpWhenGitRepositoryIsAccessible() {
        when(gitRepository.getUri()).thenReturn("https://github.com/test/repo");
        when(gitRepository.getBasedir()).thenReturn("/tmp/config");
        when(gitRepository.getDefaultLabel()).thenReturn("main");

        Health health = healthIndicator.health();

        assertEquals(Status.UP, health.getStatus());
        assertNotNull(health.getDetails().get("repository"));
    }

    @Test
    void shouldReturnDownWhenGitRepositoryIsInaccessible() {
        when(gitRepository.getUri()).thenReturn("https://github.com/test/repo");
        when(gitRepository.findOne(anyString(), anyString(), anyString()))
                .thenThrow(new RuntimeException("Connection failed"));

        Health health = healthIndicator.health();

        assertEquals(Status.DOWN, health.getStatus());
        assertNotNull(health.getDetails().get("error"));
    }
}