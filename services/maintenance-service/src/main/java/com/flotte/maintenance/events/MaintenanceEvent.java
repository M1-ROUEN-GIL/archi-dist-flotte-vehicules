package com.flotte.maintenance.events;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.flotte.maintenance.model.MaintenanceType;

import java.time.OffsetDateTime;
import java.util.UUID;

public record MaintenanceEvent(
    String eventType, // "maintenance.scheduled", "maintenance.started", "maintenance.completed", "maintenance.cancelled"
    UUID maintenanceId,
    UUID vehicleId,
    MaintenanceType type,
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    OffsetDateTime occurredAt
) {
    public static MaintenanceEvent scheduled(UUID maintenanceId, UUID vehicleId, MaintenanceType type) {
        return new MaintenanceEvent("maintenance.scheduled", maintenanceId, vehicleId, type, OffsetDateTime.now());
    }

    public static MaintenanceEvent started(UUID maintenanceId, UUID vehicleId, MaintenanceType type) {
        return new MaintenanceEvent("maintenance.started", maintenanceId, vehicleId, type, OffsetDateTime.now());
    }

    public static MaintenanceEvent completed(UUID maintenanceId, UUID vehicleId, MaintenanceType type) {
        return new MaintenanceEvent("maintenance.completed", maintenanceId, vehicleId, type, OffsetDateTime.now());
    }

    public static MaintenanceEvent cancelled(UUID maintenanceId, UUID vehicleId, MaintenanceType type) {
        return new MaintenanceEvent("maintenance.cancelled", maintenanceId, vehicleId, type, OffsetDateTime.now());
    }
}
