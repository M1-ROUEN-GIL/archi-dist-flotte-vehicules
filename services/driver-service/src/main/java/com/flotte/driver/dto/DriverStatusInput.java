package com.flotte.driver.dto;

import com.flotte.driver.models.enums.DriverStatus;
import jakarta.validation.constraints.NotNull;

public record DriverStatusInput(
		@NotNull(message = "Le statut est obligatoire")
		DriverStatus status
) {
}