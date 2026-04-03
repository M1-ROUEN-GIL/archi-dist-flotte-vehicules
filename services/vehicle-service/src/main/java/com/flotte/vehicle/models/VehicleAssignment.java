package com.flotte.vehicle.models;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "vehicle_assignments")
public class VehicleAssignment {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@Column(name = "vehicle_id", nullable = false)
	private UUID vehicleId;

	@Column(name = "driver_id", nullable = false)
	private UUID driverId;

	@Column(name = "started_at", nullable = false)
	private OffsetDateTime startedAt;

	@Column(name = "ended_at")
	private OffsetDateTime endedAt;

	@Column(columnDefinition = "TEXT")
	private String notes;

	@Column(name = "created_by", nullable = false)
	private UUID createdBy;

	@CreationTimestamp
	@Column(name = "created_at", updatable = false)
	private OffsetDateTime createdAt;

	public VehicleAssignment() {}

	// Getters & Setters
	public UUID getId() { return id; }
	public void setId(UUID id) { this.id = id; }
	public UUID getVehicleId() { return vehicleId; }
	public void setVehicleId(UUID vehicleId) { this.vehicleId = vehicleId; }
	public UUID getDriverId() { return driverId; }
	public void setDriverId(UUID driverId) { this.driverId = driverId; }
	public OffsetDateTime getStartedAt() { return startedAt; }
	public void setStartedAt(OffsetDateTime startedAt) { this.startedAt = startedAt; }
	public OffsetDateTime getEndedAt() { return endedAt; }
	public void setEndedAt(OffsetDateTime endedAt) { this.endedAt = endedAt; }
	public String getNotes() { return notes; }
	public void setNotes(String notes) { this.notes = notes; }
	public UUID getCreatedBy() { return createdBy; }
	public void setCreatedBy(UUID createdBy) { this.createdBy = createdBy; }
	public OffsetDateTime getCreatedAt() { return createdAt; }
}