package com.flotte.maintenance.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flotte.maintenance.config.SecurityConfig;
import com.flotte.maintenance.dto.MaintenanceCreateRequest;
import com.flotte.maintenance.dto.MaintenanceStatusUpdate;
import com.flotte.maintenance.model.MaintenancePriority;
import com.flotte.maintenance.model.MaintenanceRecord;
import com.flotte.maintenance.model.MaintenanceStatus;
import com.flotte.maintenance.model.MaintenanceType;
import com.flotte.maintenance.service.MaintenanceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MaintenanceController.class)
@Import(SecurityConfig.class)
@AutoConfigureMockMvc
class MaintenanceControllerTest {

	private static final String BEARER = "Bearer test-token";

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockitoBean
	private MaintenanceService service;

	@MockitoBean
	private JwtDecoder jwtDecoder;

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
	void getAllRecords_ShouldReturnOk() throws Exception {
		when(service.getAllRecords(null, null, null)).thenReturn(List.of());
		mockMvc.perform(get("/maintenance").header(HttpHeaders.AUTHORIZATION, BEARER))
				.andExpect(status().isOk());
	}

	@Test
	void getVehicleHistory_ShouldReturnOk() throws Exception {
		UUID vid = UUID.randomUUID();
		when(service.getVehicleHistory(vid)).thenReturn(List.of());
		mockMvc.perform(get("/maintenance/vehicle/{vehicleId}", vid).header(HttpHeaders.AUTHORIZATION, BEARER))
				.andExpect(status().isOk());
	}

	@Test
	void getRecordById_ShouldReturnOk() throws Exception {
		UUID id = UUID.randomUUID();
		MaintenanceRecord r = new MaintenanceRecord();
		r.setId(id);
		when(service.getRecordById(id)).thenReturn(r);
		mockMvc.perform(get("/maintenance/{id}", id).header(HttpHeaders.AUTHORIZATION, BEARER))
				.andExpect(status().isOk());
	}

	@Test
	void updateStatus_ShouldReturnOk() throws Exception {
		UUID id = UUID.randomUUID();
		MaintenanceStatusUpdate body = new MaintenanceStatusUpdate(MaintenanceStatus.IN_PROGRESS, null, null, null, null);
		when(service.updateStatus(eq(id), any())).thenReturn(new MaintenanceRecord());
		mockMvc.perform(patch("/maintenance/{id}/status", id)
						.header(HttpHeaders.AUTHORIZATION, BEARER)
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(body)))
				.andExpect(status().isOk());
	}

	@Test
	void createRecord_ShouldReturnCreated() throws Exception {
		MaintenanceCreateRequest req = new MaintenanceCreateRequest(
				UUID.randomUUID(), MaintenanceType.PREVENTIVE, MaintenancePriority.MEDIUM,
				LocalDate.now().plusDays(1), "d");
		MaintenanceRecord saved = new MaintenanceRecord();
		saved.setId(UUID.randomUUID());
		when(service.createRecord(any())).thenReturn(saved);
		mockMvc.perform(post("/maintenance")
						.header(HttpHeaders.AUTHORIZATION, BEARER)
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(req)))
				.andExpect(status().isCreated());
	}
}
