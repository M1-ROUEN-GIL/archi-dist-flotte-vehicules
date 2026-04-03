package com.flotte.vehicle.repositories;

import com.flotte.vehicle.models.VehicleAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface VehicleAssignmentRepository extends JpaRepository<VehicleAssignment, UUID> {
	// Toutes les assignations d'un véhicule
	List<VehicleAssignment> findByVehicleIdOrderByStartedAtDesc(UUID vehicleId);

	// L'assignation active (pas encore terminée)
	@Query("SELECT a FROM VehicleAssignment a WHERE a.vehicleId = :vehicleId AND a.endedAt IS NULL")
	Optional<VehicleAssignment> findActiveByVehicleId(UUID vehicleId);

}
