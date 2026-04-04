package com.flotte.vehicle.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flotte.vehicle.dto.*;
import com.flotte.vehicle.models.enums.FuelType;
import com.flotte.vehicle.models.enums.VehicleStatus;
import com.flotte.vehicle.config.SecurityConfig;
import com.flotte.vehicle.services.VehicleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.logging.OpenTelemetryLoggingAutoConfiguration;

@WebMvcTest(controllers = VehicleController.class, properties = {
        "spring.security.oauth2.resourceserver.jwt.jwk-set-uri=http://localhost:8080/realms/flotte/protocol/openid-connect/certs",
        "flotte.postgres.auto-create-databases=false"
})
@Import(SecurityConfig.class)
@AutoConfigureMockMvc
@EnableAutoConfiguration(exclude = {OpenTelemetryLoggingAutoConfiguration.class})
class VehicleControllerTest {

	private static final String BEARER = "Bearer test-token";

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private VehicleService vehicleService;

	@MockitoBean
	private JwtDecoder jwtDecoder;

	@Autowired
	private ObjectMapper objectMapper;

	@BeforeEach
	void stubJwtDecoder() {
		when(jwtDecoder.decode(anyString())).thenReturn(jwtWithRealmRoles("admin"));
	}

	private static Jwt jwtWithRealmRoles(String... roles) {
		return Jwt.withTokenValue("test-token")
			.header("alg", "none")
			.claim("realm_access", Map.of("roles", List.of(roles)))
			.issuedAt(Instant.now())
			.expiresAt(Instant.now().plusSeconds(3600))
			.build();
	}

	@Test
	void getAllVehicles_ShouldReturnList() throws Exception {
		VehicleResponse response = new VehicleResponse(UUID.randomUUID(), "AB-123-CD", "Renault", "Kangoo", FuelType.ELECTRIC, VehicleStatus.AVAILABLE, 10000, "VIN123", 500, 3.0, OffsetDateTime.now(), OffsetDateTime.now());
		when(vehicleService.getAllVehicles(any())).thenReturn(List.of(response));

		mockMvc.perform(get("/vehicles").header(HttpHeaders.AUTHORIZATION, BEARER))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].plate_number").value("AB-123-CD"));
	}

	@Test
	void getVehicleById_ShouldReturnVehicle() throws Exception {
		UUID id = UUID.randomUUID();
		VehicleResponse response = new VehicleResponse(id, "AB-123-CD", "Renault", "Kangoo", FuelType.ELECTRIC, VehicleStatus.AVAILABLE, 10000, "VIN123", 500, 3.0, OffsetDateTime.now(), OffsetDateTime.now());
		when(vehicleService.getVehicleById(id)).thenReturn(response);

		mockMvc.perform(get("/vehicles/{id}", id).header(HttpHeaders.AUTHORIZATION, BEARER))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(id.toString()));
	}

	@Test
	void createVehicle_ShouldReturnCreated() throws Exception {
		VehicleInput input = new VehicleInput("AB-123-CD", "Renault", "Kangoo", FuelType.ELECTRIC, 10000, "VIN123", 500, 3.0);
		VehicleResponse response = new VehicleResponse(UUID.randomUUID(), "AB-123-CD", "Renault", "Kangoo", FuelType.ELECTRIC, VehicleStatus.AVAILABLE, 10000, "VIN123", 500, 3.0, OffsetDateTime.now(), OffsetDateTime.now());
		when(vehicleService.createVehicle(any(VehicleInput.class))).thenReturn(response);

		mockMvc.perform(post("/vehicles")
						.header(HttpHeaders.AUTHORIZATION, BEARER)
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(input)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.plate_number").value("AB-123-CD"));
	}

	@Test
	void updateVehicle_ShouldReturnOk() throws Exception {
		UUID id = UUID.randomUUID();
		VehicleUpdate update = new VehicleUpdate("Renault", "Master", 15000);
		VehicleResponse response = new VehicleResponse(id, "AB-123-CD", "Renault", "Master", FuelType.DIESEL, VehicleStatus.AVAILABLE, 15000, "VIN123", 500, 3.0, OffsetDateTime.now(), OffsetDateTime.now());
		when(vehicleService.updateVehicle(eq(id), any(VehicleUpdate.class))).thenReturn(response);

		mockMvc.perform(put("/vehicles/{id}", id)
						.header(HttpHeaders.AUTHORIZATION, BEARER)
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(update)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.model").value("Master"));
	}

	@Test
	void updateVehicleStatus_ShouldReturnOk() throws Exception {
		when(jwtDecoder.decode(anyString())).thenReturn(jwtWithRealmRoles("technician"));
		UUID id = UUID.randomUUID();
		VehicleStatusInput statusInput = new VehicleStatusInput(VehicleStatus.IN_MAINTENANCE);
		VehicleResponse response = new VehicleResponse(id, "AB-123-CD", "Renault", "Kangoo", FuelType.ELECTRIC, VehicleStatus.IN_MAINTENANCE, 10000, "VIN123", 500, 3.0, OffsetDateTime.now(), OffsetDateTime.now());
		when(vehicleService.updateVehicleStatus(eq(id), any(VehicleStatus.class))).thenReturn(response);

		mockMvc.perform(patch("/vehicles/{id}/status", id)
						.header(HttpHeaders.AUTHORIZATION, BEARER)
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(statusInput)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("IN_MAINTENANCE"));
	}

	@Test
	void deleteVehicle_ShouldReturnNoContent() throws Exception {
		UUID id = UUID.randomUUID();

		mockMvc.perform(delete("/vehicles/{id}", id).header(HttpHeaders.AUTHORIZATION, BEARER))
				.andExpect(status().isNoContent());

		verify(vehicleService).deleteVehicle(id);
	}

	@Test
	void getAssignments_ShouldReturnList() throws Exception {
		UUID id = UUID.randomUUID();
		AssignmentResponse response = new AssignmentResponse(UUID.randomUUID(), id, UUID.randomUUID(), OffsetDateTime.now(), null, "Notes", UUID.randomUUID(), OffsetDateTime.now());
		when(vehicleService.getAssignments(id)).thenReturn(List.of(response));

		mockMvc.perform(get("/vehicles/{id}/assignments", id).header(HttpHeaders.AUTHORIZATION, BEARER))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].vehicle_id").value(id.toString()));
	}

	@Test
	void createAssignment_ShouldReturnCreated() throws Exception {
		UUID id = UUID.randomUUID();
		AssignmentInput input = new AssignmentInput(UUID.randomUUID(), "Notes", UUID.randomUUID());
		AssignmentResponse response = new AssignmentResponse(UUID.randomUUID(), id, input.driverId(), OffsetDateTime.now(), null, "Notes", input.createdBy(), OffsetDateTime.now());
		when(vehicleService.createAssignment(eq(id), any(AssignmentInput.class))).thenReturn(response);

		mockMvc.perform(post("/vehicles/{id}/assignments", id)
						.header(HttpHeaders.AUTHORIZATION, BEARER)
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(input)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.driver_id").value(input.driverId().toString()));
	}

	@Test
	void endCurrentAssignment_ShouldReturnOk() throws Exception {
		UUID id = UUID.randomUUID();
		AssignmentResponse response = new AssignmentResponse(UUID.randomUUID(), id, UUID.randomUUID(), OffsetDateTime.now(), OffsetDateTime.now(), "Notes", UUID.randomUUID(), OffsetDateTime.now());
		when(vehicleService.endCurrentAssignment(id)).thenReturn(response);

		mockMvc.perform(delete("/vehicles/{id}/assignments/current", id).header(HttpHeaders.AUTHORIZATION, BEARER))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.ended_at").exists());
	}
}
