package com.flotte.maintenance.events;

import com.flotte.maintenance.model.MaintenanceType;
import com.flotte.vehicle.events.StatusChange;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record MaintenancePayload(
	UUID recordId,
	UUID vehicleId,
	MaintenanceType type,
	StatusChange status,
	UUID technicianId,
	LocalDate completedDate,
	BigDecimal costEur,
	Integer nextServiceKm
) {}
