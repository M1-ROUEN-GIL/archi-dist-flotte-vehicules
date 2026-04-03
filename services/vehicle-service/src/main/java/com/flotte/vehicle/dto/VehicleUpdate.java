package com.flotte.vehicle.dto;

import jakarta.validation.constraints.*;

public record VehicleUpdate(
		String brand,
		String model,

		@Min(value = 0, message = "Le kilométrage ne peut pas être négatif")
		Integer mileageKm
) {}