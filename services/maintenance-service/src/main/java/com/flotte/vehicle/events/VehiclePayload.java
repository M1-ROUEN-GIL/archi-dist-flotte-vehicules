package com.flotte.vehicle.events;

import java.util.UUID;

public record VehiclePayload(
		UUID vehicleId,
		String plateNumber,
		String brand,
		String model,
		String fuelType,
		Object status, // On met Object car ça peut être une String ("AVAILABLE") ou un StatusChange
		Integer mileageKm
) {}