package com.flotte.maintenance.dto;

import com.flotte.maintenance.model.MaintenancePriority;
import com.flotte.maintenance.model.MaintenanceType;

import java.time.LocalDate;
import java.util.UUID;

public record MaintenanceCreateRequest(
        UUID vehicleId,
        MaintenanceType type,
        MaintenancePriority priority,
        LocalDate scheduledDate,
        String description
) {}
