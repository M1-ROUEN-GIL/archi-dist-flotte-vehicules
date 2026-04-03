package com.flotte.vehicle.repositories;

import com.flotte.vehicle.models.Vehicle;
import com.flotte.vehicle.models.enums.VehicleStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, UUID> {

	// Fini le Pageable et le Page<Vehicle>, on veut juste une Liste !
	@Query("SELECT v FROM Vehicle v WHERE v.deletedAt IS NULL")
	List<Vehicle> findAllActive();

	@Query("SELECT v FROM Vehicle v WHERE v.id = :id AND v.deletedAt IS NULL")
	Optional<Vehicle> findByIdActive(UUID id);

	List<Vehicle> findByStatus(VehicleStatus status);

	boolean existsByPlateNumber(String plateNumber);
}