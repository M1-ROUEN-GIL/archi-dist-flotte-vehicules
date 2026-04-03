package com.flotte.vehicle.models;


import com.flotte.vehicle.models.enums.FuelType;
import com.flotte.vehicle.models.enums.VehicleStatus;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "vehicles")
public class Vehicle {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@Column(name = "plate_number", nullable = false, unique = true, length = 20)
	private String plateNumber;

	@Column(nullable = false, length = 100)
	private String brand;

	@Column(nullable = false, length = 100)
	private String model;

	@Enumerated(EnumType.STRING)
	@JdbcTypeCode(SqlTypes.NAMED_ENUM)
	@Column(name= "fuel_type", nullable = false)
	private FuelType fuelType;

	@Column(name = "mileage_km", nullable = false)
	private Integer mileageKm;

	@Enumerated(EnumType.STRING)
	@JdbcTypeCode(SqlTypes.NAMED_ENUM)
	@Column(name = "status", nullable = false)
	private VehicleStatus status = VehicleStatus.AVAILABLE;
	@Column(unique = true, length = 17)
	private String vin;

	@Column(name = "payload_capacity_kg", nullable = false)
	private Integer payloadCapacityKg = 1000;

	@Column(name = "cargo_volume_m3", nullable = false)
	private Double cargoVolumeM3 = 10.0;

	// Pour le JSONB, on utilise columnDefinition pour que Hibernate ne râle pas
	@JdbcTypeCode(SqlTypes.JSON) //  Indique à Hibernate de convertir ça en JSONB pour Postgres
	@Column(name = "metadata")
	private String metadata = "{}";

	@CreationTimestamp
	@Column(name = "created_at", updatable = false)
	private OffsetDateTime createdAt;

	@UpdateTimestamp
	@Column(name = "updated_at")
	private OffsetDateTime updatedAt;

	@Column(name = "deleted_at")
	private OffsetDateTime deletedAt; // Pour le Soft Delete demandé dans l'OpenAPI

	// Constructeur vide obligatoire pour JPA/Hibernate
	public Vehicle() {
	}

	// Getters et Setters

	public UUID getId() { return id; }
	public void setId(UUID id) { this.id = id; }

	public String getPlateNumber() { return plateNumber; }
	public void setPlateNumber(String plateNumber) { this.plateNumber = plateNumber; }

	public String getBrand() { return brand; }
	public void setBrand(String brand) { this.brand = brand; }

	public String getModel() { return model; }
	public void setModel(String model) { this.model = model; }

	public FuelType getFuelType() { return fuelType; }
	public void setFuelType(FuelType fuelType) { this.fuelType = fuelType; }

	public Integer getMileageKm() { return mileageKm; }
	public void setMileageKm(Integer mileageKm) { this.mileageKm = mileageKm; }

	public VehicleStatus getStatus() { return status; }
	public void setStatus(VehicleStatus status) { this.status = status; }

	public String getVin() { return vin; }
	public void setVin(String vin) { this.vin = vin; }

	public Integer getPayloadCapacityKg() { return payloadCapacityKg; }
	public void setPayloadCapacityKg(Integer payloadCapacityKg) { this.payloadCapacityKg = payloadCapacityKg; }

	public Double getCargoVolumeM3() { return cargoVolumeM3; }
	public void setCargoVolumeM3(Double cargoVolumeM3) { this.cargoVolumeM3 = cargoVolumeM3; }

	public String getMetadata() { return metadata; }
	public void setMetadata(String metadata) { this.metadata = metadata; }

	public OffsetDateTime getCreatedAt() { return createdAt; }
	public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }

	public OffsetDateTime getUpdatedAt() { return updatedAt; }
	public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }

	public OffsetDateTime getDeletedAt() { return deletedAt; }
	public void setDeletedAt(OffsetDateTime deletedAt) { this.deletedAt = deletedAt; }

}
