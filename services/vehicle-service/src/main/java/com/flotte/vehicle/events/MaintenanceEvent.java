package com.flotte.vehicle.events;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.OffsetDateTime;
import java.util.UUID;

public record MaintenanceEvent(
    String eventType,
    UUID maintenanceId,
    UUID vehicleId,
    String type,
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    OffsetDateTime occurredAt
) {
}
