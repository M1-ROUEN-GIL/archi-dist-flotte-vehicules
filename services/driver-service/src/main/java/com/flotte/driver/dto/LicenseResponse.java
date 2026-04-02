package com.flotte.driver.dto;

import com.flotte.driver.models.enums.LicenseCategory;
import java.time.LocalDate;
import java.util.UUID;

public record LicenseResponse(
        UUID id,
        UUID driverId,
        String licenseNumber,
        LicenseCategory category,
        LocalDate issuedDate,
        LocalDate expiryDate,
        String country,
        Boolean isValid
) {
}