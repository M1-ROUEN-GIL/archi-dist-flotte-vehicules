package com.flotte.maintenance.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flotte.maintenance.MaintenanceSpringBootTest;
import com.flotte.maintenance.dto.MaintenanceCreateRequest;
import com.flotte.maintenance.model.MaintenancePriority;
import com.flotte.maintenance.model.MaintenanceType;
import com.flotte.maintenance.service.MaintenanceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@MaintenanceSpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class MaintenanceControllerIntegrationTest {

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
	void defaultAdminJwt() {
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
	void createRecord_WhenAdmin_ShouldReturnCreated() throws Exception {
		MaintenanceCreateRequest request = new MaintenanceCreateRequest(
				UUID.randomUUID(), MaintenanceType.PREVENTIVE, MaintenancePriority.MEDIUM,
				LocalDate.now().plusDays(10), "Test description");

		mockMvc.perform(post("/maintenance")
				.header(HttpHeaders.AUTHORIZATION, BEARER)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isCreated());
	}

	@Test
	void createRecord_WhenTechnician_ShouldReturnForbidden() throws Exception {
		when(jwtDecoder.decode(anyString())).thenReturn(jwtWithRealmRoles("technician"));
		MaintenanceCreateRequest request = new MaintenanceCreateRequest(
				UUID.randomUUID(), MaintenanceType.PREVENTIVE, MaintenancePriority.MEDIUM,
				LocalDate.now().plusDays(10), "Test description");

		mockMvc.perform(post("/maintenance")
				.header(HttpHeaders.AUTHORIZATION, BEARER)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isForbidden());
	}

	@Test
	void getVehicleHistory_WhenTechnician_ShouldReturnOk() throws Exception {
		when(jwtDecoder.decode(anyString())).thenReturn(jwtWithRealmRoles("technician"));
		mockMvc.perform(get("/maintenance/vehicle/" + UUID.randomUUID())
				.header(HttpHeaders.AUTHORIZATION, BEARER))
				.andExpect(status().isOk());
	}

	@Test
	void getVehicleHistory_WhenNotAuthenticated_ShouldReturnUnauthorized() throws Exception {
		mockMvc.perform(get("/maintenance/vehicle/" + UUID.randomUUID()))
				.andExpect(status().isUnauthorized());
	}
}
