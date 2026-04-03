package com.flotte.vehicle.dto;

import com.flotte.vehicle.models.enums.FuelType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record VehicleInput(
		@NotBlank(message = "La plaque d'immatriculation est obligatoire")
		String plateNumber,

		@NotBlank(message = "La marque est obligatoire")
		String brand,

		@NotBlank(message = "Le modèle est obligatoire")
		String model,

		@NotNull(message = "Le type de carburant est obligatoire")
		FuelType fuelType,

		@NotNull(message = "Le kilométrage est obligatoire")
		@Min(value = 0, message = "Le kilométrage ne peut pas être négatif")
		Integer mileageKm,

		String vin,

		@NotNull(message = "La charge utile est obligatoire")
		@Positive(message = "La charge utile doit être supérieure à 0")
		Integer payloadCapacityKg,

		@NotNull(message = "Le volume de chargement est obligatoire")
		@Positive(message = "Le volume doit être supérieur à 0")
		Double cargoVolumeM3
) {}