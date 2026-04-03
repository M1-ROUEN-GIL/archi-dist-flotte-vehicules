package com.flotte.driver.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flotte.driver.dto.DriverInput;
import com.flotte.driver.services.DriverService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@org.springframework.test.context.ActiveProfiles("test")
public class DriverControllerIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockBean
	private DriverService driverService;

	@Test
	@WithMockUser(roles = "admin")
	void createDriver_WhenAdmin_ShouldReturnCreated() throws Exception {
		DriverInput input = new DriverInput(UUID.randomUUID(), "John", "Doe", "john@example.com", "123456", "EMP001");

		mockMvc.perform(post("/drivers")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(input)))
				.andExpect(status().isCreated());
	}

	@Test
	@WithMockUser(roles = "user")
	void createDriver_WhenUser_ShouldReturnForbidden() throws Exception {
		DriverInput input = new DriverInput(UUID.randomUUID(), "John", "Doe", "john@example.com", "123456", "EMP001");

		mockMvc.perform(post("/drivers")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(input)))
				.andExpect(status().isForbidden());
	}

	@Test
	@WithMockUser(roles = "admin")
	void getAllDrivers_WhenAdmin_ShouldReturnOk() throws Exception {
		mockMvc.perform(get("/drivers"))
				.andExpect(status().isOk());
	}

	@Test
	void getAllDrivers_WhenNotAuthenticated_ShouldReturnUnauthorized() throws Exception {
		mockMvc.perform(get("/drivers"))
				.andExpect(status().isUnauthorized());
	}
}
