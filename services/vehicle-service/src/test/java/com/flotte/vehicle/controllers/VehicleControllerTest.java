package com.flotte.vehicle.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flotte.vehicle.dto.*;
import com.flotte.vehicle.models.enums.FuelType;
import com.flotte.vehicle.models.enums.VehicleStatus;
import com.flotte.vehicle.services.VehicleService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(VehicleController.class)
@AutoConfigureMockMvc(addFilters = false)
class VehicleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private VehicleService vehicleService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getAllVehicles_ShouldReturnList() throws Exception {
        VehicleResponse response = new VehicleResponse(UUID.randomUUID(), "AB-123-CD", "Renault", "Kangoo", FuelType.electric, VehicleStatus.available, 10000, "VIN123", 500, 3.0, OffsetDateTime.now(), OffsetDateTime.now());
        when(vehicleService.getAllVehicles()).thenReturn(List.of(response));

        mockMvc.perform(get("/vehicles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].plateNumber").value("AB-123-CD"));
    }

    @Test
    void getVehicleById_ShouldReturnVehicle() throws Exception {
        UUID id = UUID.randomUUID();
        VehicleResponse response = new VehicleResponse(id, "AB-123-CD", "Renault", "Kangoo", FuelType.electric, VehicleStatus.available, 10000, "VIN123", 500, 3.0, OffsetDateTime.now(), OffsetDateTime.now());
        when(vehicleService.getVehicleById(id)).thenReturn(response);

        mockMvc.perform(get("/vehicles/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()));
    }

    @Test
    void createVehicle_ShouldReturnCreated() throws Exception {
        VehicleInput input = new VehicleInput("AB-123-CD", "Renault", "Kangoo", FuelType.electric, 10000, "VIN123", 500, 3.0);
        VehicleResponse response = new VehicleResponse(UUID.randomUUID(), "AB-123-CD", "Renault", "Kangoo", FuelType.electric, VehicleStatus.available, 10000, "VIN123", 500, 3.0, OffsetDateTime.now(), OffsetDateTime.now());
        when(vehicleService.createVehicle(any(VehicleInput.class))).thenReturn(response);

        mockMvc.perform(post("/vehicles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.plateNumber").value("AB-123-CD"));
    }

    @Test
    void updateVehicle_ShouldReturnOk() throws Exception {
        UUID id = UUID.randomUUID();
        VehicleUpdate update = new VehicleUpdate("Renault", "Master", 15000);
        VehicleResponse response = new VehicleResponse(id, "AB-123-CD", "Renault", "Master", FuelType.diesel, VehicleStatus.available, 15000, "VIN123", 500, 3.0, OffsetDateTime.now(), OffsetDateTime.now());
        when(vehicleService.updateVehicle(eq(id), any(VehicleUpdate.class))).thenReturn(response);

        mockMvc.perform(put("/vehicles/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.model").value("Master"));
    }

    @Test
    void updateVehicleStatus_ShouldReturnOk() throws Exception {
        UUID id = UUID.randomUUID();
        VehicleStatusInput statusInput = new VehicleStatusInput(VehicleStatus.in_maintenance);
        VehicleResponse response = new VehicleResponse(id, "AB-123-CD", "Renault", "Kangoo", FuelType.electric, VehicleStatus.in_maintenance, 10000, "VIN123", 500, 3.0, OffsetDateTime.now(), OffsetDateTime.now());
        when(vehicleService.updateVehicleStatus(eq(id), any(VehicleStatus.class))).thenReturn(response);

        mockMvc.perform(patch("/vehicles/{id}/status", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(statusInput)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("in_maintenance"));
    }

    @Test
    void deleteVehicle_ShouldReturnNoContent() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(delete("/vehicles/{id}", id))
                .andExpect(status().isNoContent());

        verify(vehicleService).deleteVehicle(id);
    }

    @Test
    void getAssignments_ShouldReturnList() throws Exception {
        UUID id = UUID.randomUUID();
        AssignmentResponse response = new AssignmentResponse(UUID.randomUUID(), id, UUID.randomUUID(), OffsetDateTime.now(), null, "Notes", UUID.randomUUID(), OffsetDateTime.now());
        when(vehicleService.getAssignments(id)).thenReturn(List.of(response));

        mockMvc.perform(get("/vehicles/{id}/assignments", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].vehicleId").value(id.toString()));
    }

    @Test
    void createAssignment_ShouldReturnCreated() throws Exception {
        UUID id = UUID.randomUUID();
        AssignmentInput input = new AssignmentInput(UUID.randomUUID(), "Notes", UUID.randomUUID());
        AssignmentResponse response = new AssignmentResponse(UUID.randomUUID(), id, input.driverId(), OffsetDateTime.now(), null, "Notes", input.createdBy(), OffsetDateTime.now());
        when(vehicleService.createAssignment(eq(id), any(AssignmentInput.class))).thenReturn(response);

        mockMvc.perform(post("/vehicles/{id}/assignments", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.driverId").value(input.driverId().toString()));
    }

    @Test
    void endCurrentAssignment_ShouldReturnOk() throws Exception {
        UUID id = UUID.randomUUID();
        AssignmentResponse response = new AssignmentResponse(UUID.randomUUID(), id, UUID.randomUUID(), OffsetDateTime.now(), OffsetDateTime.now(), "Notes", UUID.randomUUID(), OffsetDateTime.now());
        when(vehicleService.endCurrentAssignment(id)).thenReturn(response);

        mockMvc.perform(delete("/vehicles/{id}/assignments/current", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.endedAt").exists());
    }
}
