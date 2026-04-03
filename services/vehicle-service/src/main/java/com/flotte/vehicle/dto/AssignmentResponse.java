package com.flotte.vehicle.dto;

import com.flotte.vehicle.models.VehicleAssignment;
import java.time.OffsetDateTime;
import java.util.UUID;

public record AssignmentResponse(
		UUID id,
		UUID vehicleId,
		UUID driverId,
		OffsetDateTime startedAt,
		OffsetDateTime endedAt,
		String notes,
		UUID createdBy,
		OffsetDateTime createdAt
) {
	public static AssignmentResponse fromEntity(VehicleAssignment a) {
		return new AssignmentResponse(
				a.getId(),
				a.getVehicleId(),
				a.getDriverId(),
				a.getStartedAt(),
				a.getEndedAt(),
				a.getNotes(),
				a.getCreatedBy(),
				a.getCreatedAt()
		);
	}
}