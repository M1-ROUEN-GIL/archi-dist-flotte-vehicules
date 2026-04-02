package com.flotte.vehicle.events;

import java.time.OffsetDateTime;
import java.util.UUID;

public record AssignmentPayload(
        UUID assignmentId,
        UUID vehicleId,
        UUID driverId,
        OffsetDateTime startedAt,
        OffsetDateTime endedAt,
        String notes
) {}