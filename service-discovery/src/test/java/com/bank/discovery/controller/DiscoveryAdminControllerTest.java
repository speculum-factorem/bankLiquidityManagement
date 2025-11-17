package com.bank.discovery.controller;

import com.netflix.discovery.shared.Application;
import com.netflix.discovery.shared.Applications;
import com.netflix.eureka.registry.PeerAwareInstanceRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostprocessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DiscoveryAdminController.class)
class DiscoveryAdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PeerAwareInstanceRegistry instanceRegistry;

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldGetRegisteredApplications() throws Exception {
        Applications applications = new Applications();
        Application app = new Application("LIQUIDITY-SERVICE");
        applications.addApplication(app);

        when(instanceRegistry.getApplications()).thenReturn(applications);

        mockMvc.perform(get("/admin/discovery/applications"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldGetApplication() throws Exception {
        Application app = new Application("LIQUIDITY-SERVICE");

        when(instanceRegistry.getApplication("LIQUIDITY-SERVICE")).thenReturn(app);

        mockMvc.perform(get("/admin/discovery/applications/liquidity-service"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("LIQUIDITY-SERVICE"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldReturnErrorWhenApplicationNotFound() throws Exception {
        when(instanceRegistry.getApplication("UNKNOWN-SERVICE")).thenReturn(null);

        mockMvc.perform(get("/admin/discovery/applications/unknown-service"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldGetDiscoveryStats() throws Exception {
        Applications applications = new Applications();
        Application app1 = new Application("LIQUIDITY-SERVICE");
        Application app2 = new Application("RISK-SERVICE");
        applications.addApplication(app1);
        applications.addApplication(app2);

        when(instanceRegistry.getApplications()).thenReturn(applications);

        mockMvc.perform(get("/admin/discovery/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalApplications").value(2));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldUpdateInstanceStatus() throws Exception {
        mockMvc.perform(post("/admin/discovery/applications/test-app/instances/test-instance/status")
                        .param("status", "UP")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").exists());
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldDenyAccessWithoutAdminRole() throws Exception {
        mockMvc.perform(get("/admin/discovery/applications"))
                .andExpect(status().isForbidden());
    }
}