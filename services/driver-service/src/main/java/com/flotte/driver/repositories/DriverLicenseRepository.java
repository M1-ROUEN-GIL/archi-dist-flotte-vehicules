package com.flotte.driver.repositories;

import com.flotte.driver.models.DriverLicense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DriverLicenseRepository  extends JpaRepository<DriverLicense, UUID> {
    // Trouver tous les permis d'un chauffeur spécifique
    List<DriverLicense> findByDriverId(UUID driverId);

    // Vérifier si un numéro de permis existe déjà (très utile pour éviter les doublons à la création !)
    boolean existsByLicenseNumber(String licenseNumber);

    // Trouver un permis spécifique par son numéro
    Optional<DriverLicense> findByLicenseNumber(String licenseNumber);

    // Spring comprend tout seul qu'il doit chercher par le champ "expiryDate" !
    List<DriverLicense> findByExpiryDate(LocalDate date);
}
