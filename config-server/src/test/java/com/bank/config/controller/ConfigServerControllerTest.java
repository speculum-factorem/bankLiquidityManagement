package com.bank.config.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ConfigServerController.class)
class ConfigServerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private org.springframework.cloud.config.server.environment.EnvironmentController environmentController;

    @Test
    void shouldReturnConfigServerInfo() throws Exception {
        mockMvc.perform(get("/admin/info"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("running"))
                .andExpect(jsonPath("$.name").exists());
    }

    @Test
    void shouldTriggerConfigRefresh() throws Exception {
        mockMvc.perform(post("/admin/refresh/liquidity-service")
                        .param("profile", "default"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.application").value("liquidity-service"))
                .andExpect(jsonPath("$.profile").value("default"))
                .andExpect(jsonPath("$.status").value("refresh_triggered"));
    }

    @Test
    void shouldReturnDetailedHealth() throws Exception {
        mockMvc.perform(get("/admin/health/detailed"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.components.configServer.status").value("UP"));
    }
}