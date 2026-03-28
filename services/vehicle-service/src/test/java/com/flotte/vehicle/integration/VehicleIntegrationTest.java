package com.flotte.vehicle.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flotte.vehicle.dto.*;
import com.flotte.vehicle.models.enums.FuelType;
import com.flotte.vehicle.models.enums.VehicleStatus;
import com.flotte.vehicle.events.producers.VehicleEventProducer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;MODE=PostgreSQL",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
    "spring.kafka.enabled=false"
})
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class VehicleIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private VehicleEventProducer eventProducer;

    @Test
    void testVehicleCrudFlow() throws Exception {
        // 1. Create a vehicle
        VehicleInput input = new VehicleInput("ZZ-999-ZZ", "Tesla", "Model 3", FuelType.electric, 0, "VIN_TEST_INTEG", 400, 2.0);
        
        String responseJson = mockMvc.perform(post("/vehicles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.plateNumber").value("ZZ-999-ZZ"))
                .andReturn().getResponse().getContentAsString();
        
        VehicleResponse createdVehicle = objectMapper.readValue(responseJson, VehicleResponse.class);
        UUID vehicleId = createdVehicle.id();

        // 2. Get vehicle by ID
        mockMvc.perform(get("/vehicles/{id}", vehicleId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.brand").value("Tesla"));

        // 3. Update vehicle
        VehicleUpdate update = new VehicleUpdate("Tesla", "Model S", 100);
        mockMvc.perform(put("/vehicles/{id}", vehicleId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.model").value("Model S"));

        // 4. List all vehicles
        mockMvc.perform(get("/vehicles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id == '" + vehicleId + "')]").exists());

        // 5. Delete vehicle
        mockMvc.perform(delete("/vehicles/{id}", vehicleId))
                .andExpect(status().isNoContent());
        
        // 6. Verify it's gone from active list
        mockMvc.perform(get("/vehicles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id == '" + vehicleId + "')]").doesNotExist());
    }

    @Test
    void testAssignmentFlow() throws Exception {
        // 1. Create a vehicle
        VehicleInput input = new VehicleInput("AA-111-AA", "Renault", "Zoe", FuelType.electric, 0, "VIN_ASSIGN", 300, 1.5);
        String vehicleJson = mockMvc.perform(post("/vehicles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andReturn().getResponse().getContentAsString();
        VehicleResponse vehicle = objectMapper.readValue(vehicleJson, VehicleResponse.class);
        UUID vehicleId = vehicle.id();

        // 2. Create an assignment
        UUID driverId = UUID.randomUUID();
        UUID createdBy = UUID.randomUUID();
        AssignmentInput assignmentInput = new AssignmentInput(driverId, "Test Assignment", createdBy);
        
        mockMvc.perform(post("/vehicles/{id}/assignments", vehicleId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(assignmentInput)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.driverId").value(driverId.toString()));

        // 3. Verify vehicle status changed to on_delivery
        mockMvc.perform(get("/vehicles/{id}", vehicleId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("on_delivery"));

        // 4. End assignment
        mockMvc.perform(delete("/vehicles/{id}/assignments/current", vehicleId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.endedAt").exists());

        // 5. Verify vehicle status back to available
        mockMvc.perform(get("/vehicles/{id}", vehicleId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("available"));
    }
}
