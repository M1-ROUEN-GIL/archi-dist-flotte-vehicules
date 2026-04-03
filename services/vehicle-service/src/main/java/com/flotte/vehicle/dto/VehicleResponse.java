package com.flotte.vehicle.dto;

import com.flotte.vehicle.models.enums.FuelType;
import com.flotte.vehicle.models.enums.VehicleStatus;
import java.time.OffsetDateTime;
import java.util.UUID;

public record VehicleResponse(
		UUID id,
		String plateNumber,
		String brand,
		String model,
		FuelType fuelType,
		VehicleStatus status,
		Integer mileageKm,
		String vin,
		Integer payloadCapacityKg,
		Double cargoVolumeM3,
		OffsetDateTime createdAt,
		OffsetDateTime updatedAt
) {}