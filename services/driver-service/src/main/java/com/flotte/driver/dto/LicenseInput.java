package com.flotte.driver.dto;

import com.flotte.driver.models.enums.LicenseCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record LicenseInput(
        @NotBlank(message = "Le numéro de permis est obligatoire")
        String licenseNumber,

        @NotNull(message = "La catégorie est obligatoire")
        LicenseCategory category,

        @NotNull(message = "La date de délivrance est obligatoire")
        LocalDate issuedDate,

        @NotNull(message = "La date d'expiration est obligatoire")
        LocalDate expiryDate,

        @NotBlank(message = "Le pays est obligatoire")
        String country
) {
}