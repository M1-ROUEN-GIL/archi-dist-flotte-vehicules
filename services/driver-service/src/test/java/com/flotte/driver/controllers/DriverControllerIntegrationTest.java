package com.flotte.driver.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flotte.driver.dto.DriverInput;
import com.flotte.driver.services.DriverService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@org.springframework.test.context.ActiveProfiles("test")
public class DriverControllerIntegrationTest {

	private static final String BEARER = "Bearer test-token";

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockBean
	private DriverService driverService;

	@MockBean
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
	void createDriver_WhenAdmin_ShouldReturnCreated() throws Exception {
		DriverInput input = new DriverInput(UUID.randomUUID(), "John", "Doe", "john@example.com", "123456", "EMP001");

		mockMvc.perform(post("/drivers")
				.header(HttpHeaders.AUTHORIZATION, BEARER)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(input)))
				.andExpect(status().isCreated());
	}

	@Test
	void createDriver_WhenUser_ShouldReturnForbidden() throws Exception {
		when(jwtDecoder.decode(anyString())).thenReturn(jwtWithRealmRoles("user"));
		DriverInput input = new DriverInput(UUID.randomUUID(), "John", "Doe", "john@example.com", "123456", "EMP001");

		mockMvc.perform(post("/drivers")
				.header(HttpHeaders.AUTHORIZATION, BEARER)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(input)))
				.andExpect(status().isForbidden());
	}

	@Test
	void getAllDrivers_WhenAdmin_ShouldReturnOk() throws Exception {
		mockMvc.perform(get("/drivers").header(HttpHeaders.AUTHORIZATION, BEARER))
				.andExpect(status().isOk());
	}

	@Test
	void getAllDrivers_WhenNotAuthenticated_ShouldReturnUnauthorized() throws Exception {
		mockMvc.perform(get("/drivers"))
				.andExpect(status().isUnauthorized());
	}
}
