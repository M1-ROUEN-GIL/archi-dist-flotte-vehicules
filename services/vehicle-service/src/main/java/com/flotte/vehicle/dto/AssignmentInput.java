package com.flotte.vehicle.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record AssignmentInput(
		@NotNull(message = "Le driver_id est obligatoire")
		UUID driverId,

		String notes,

		// Pour l'instant on met un UUID fixe, en S6 ce sera le user Keycloak
		@NotNull(message = "Le created_by est obligatoire")
		UUID createdBy
) {}