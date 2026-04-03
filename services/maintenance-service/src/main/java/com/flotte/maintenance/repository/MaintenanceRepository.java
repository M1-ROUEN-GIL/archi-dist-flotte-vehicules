package com.flotte.maintenance.repository;

import com.flotte.maintenance.model.MaintenancePriority;
import com.flotte.maintenance.model.MaintenanceRecord;
import com.flotte.maintenance.model.MaintenanceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MaintenanceRepository extends JpaRepository<MaintenanceRecord, UUID> {
	List<MaintenanceRecord> findByVehicleId(UUID vehicleId);
	List<MaintenanceRecord> findByStatus(MaintenanceStatus status);
	List<MaintenanceRecord> findByVehicleIdAndStatus(UUID vehicleId, MaintenanceStatus status);
	List<MaintenanceRecord> findByVehicleIdAndPriority(UUID vehicleId, MaintenancePriority priority);
}
