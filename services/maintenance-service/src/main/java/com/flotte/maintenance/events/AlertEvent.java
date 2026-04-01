package com.flotte.maintenance.events;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.OffsetDateTime;
import java.util.UUID;

public record AlertEvent(
    String alertType, // MAINTENANCE_OVERDUE, etc.
    UUID vehicleId,
    String severity,  // INFO, WARNING, HIGH, CRITICAL
    String message,
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    OffsetDateTime occurredAt
) {
    public static AlertEvent overdue(UUID vehicleId, String message) {
        return new AlertEvent("MAINTENANCE_OVERDUE", vehicleId, "WARNING", message, OffsetDateTime.now());
    }

    public static AlertEvent preventive(UUID vehicleId, String message) {
        return new AlertEvent("MAINTENANCE_OVERDUE", vehicleId, "INFO", message, OffsetDateTime.now());
    }
}
