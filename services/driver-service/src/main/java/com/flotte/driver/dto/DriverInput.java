package com.flotte.driver.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record DriverInput(
		@NotNull(message = "Le keycloak_user_id est obligatoire")
		UUID keycloakUserId,

		@NotBlank(message = "Le prénom est obligatoire")
		String firstName,

		@NotBlank(message = "Le nom est obligatoire")
		String lastName,

		@NotBlank(message = "L'email est obligatoire")
		@Email(message = "L'email doit être valide")
		String email,

		String phone,
		String employeeId
) {
}