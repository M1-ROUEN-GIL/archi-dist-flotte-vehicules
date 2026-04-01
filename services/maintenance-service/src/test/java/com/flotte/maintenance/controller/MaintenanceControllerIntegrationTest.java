package com.flotte.maintenance.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flotte.maintenance.dto.MaintenanceCreateRequest;
import com.flotte.maintenance.model.MaintenancePriority;
import com.flotte.maintenance.model.MaintenanceType;
import com.flotte.maintenance.service.MaintenanceService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@org.springframework.test.context.ActiveProfiles("test")
class MaintenanceControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private MaintenanceService service;

    @Test
    @WithMockUser(roles = "admin")
    void createRecord_WhenAdmin_ShouldReturnCreated() throws Exception {
        MaintenanceCreateRequest request = new MaintenanceCreateRequest(
                UUID.randomUUID(), MaintenanceType.PREVENTIVE, MaintenancePriority.MEDIUM,
                LocalDate.now().plusDays(10), "Test description");

        mockMvc.perform(post("/api/maintenance")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = "technician")
    void createRecord_WhenTechnician_ShouldReturnForbidden() throws Exception {
        MaintenanceCreateRequest request = new MaintenanceCreateRequest(
                UUID.randomUUID(), MaintenanceType.PREVENTIVE, MaintenancePriority.MEDIUM,
                LocalDate.now().plusDays(10), "Test description");

        mockMvc.perform(post("/api/maintenance")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "technician")
    void getVehicleHistory_WhenTechnician_ShouldReturnOk() throws Exception {
        mockMvc.perform(get("/api/maintenance/vehicle/" + UUID.randomUUID()))
                .andExpect(status().isOk());
    }

    @Test
    void getVehicleHistory_WhenNotAuthenticated_ShouldReturnUnauthorized() throws Exception {
        mockMvc.perform(get("/api/maintenance/vehicle/" + UUID.randomUUID()))
                .andExpect(status().isUnauthorized());
    }
}
