package com.flotte.driver.dto;

import com.flotte.driver.models.enums.DriverStatus;
import java.time.OffsetDateTime;
import java.util.UUID;

public record DriverResponse(
		UUID id,
		UUID keycloakUserId,
		String firstName,
		String lastName,
		String email,
		String phone,
		String employeeId,
		DriverStatus status,
		OffsetDateTime createdAt,
		OffsetDateTime updatedAt
) {
}