package com.flotte.maintenance.dto;

import com.flotte.maintenance.model.MaintenanceStatus;

import java.math.BigDecimal;

public record MaintenanceStatusUpdate(
		MaintenanceStatus status,
		BigDecimal costEur,
		String notes,
		Integer mileageAtService,
		Integer nextServiceKm
) {}
