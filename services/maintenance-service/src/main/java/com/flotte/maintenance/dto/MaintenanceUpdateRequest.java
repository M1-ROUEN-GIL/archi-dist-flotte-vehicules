package com.flotte.maintenance.dto;

import com.flotte.maintenance.model.MaintenancePriority;
import com.flotte.maintenance.model.MaintenanceStatus;

import java.time.LocalDate;

public record MaintenanceUpdateRequest(
	String description,
	MaintenancePriority priority,
	LocalDate scheduledDate,
	MaintenanceStatus status
) {
}
