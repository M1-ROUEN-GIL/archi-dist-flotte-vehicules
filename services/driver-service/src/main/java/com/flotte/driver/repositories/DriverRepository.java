package com.flotte.driver.repositories;

import com.flotte.driver.models.Driver;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface DriverRepository extends JpaRepository<Driver, UUID> {
    Optional<Driver> findByEmail(String email);

    Optional<Driver> findByKeycloakUserId(UUID keycloakUserId);

    boolean existsByEmail(String email);
}
