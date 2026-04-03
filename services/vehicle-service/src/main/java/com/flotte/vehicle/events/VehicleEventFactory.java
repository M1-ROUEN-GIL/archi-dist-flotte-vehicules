package com.flotte.vehicle.events;

import com.flotte.maintenance.events.MaintenancePayload;
import java.time.OffsetDateTime;
import java.util.UUID;

public class VehicleEventFactory {

	private static final String VERSION = "1.0";

	// --- TOPIC 1 : VÉHICULES ---

	public static KafkaEventEnvelope<VehiclePayload> vehicleCreated(UUID vehicleId, String plateNumber, String brand, String model, String fuelType, String status, Integer mileage) {
		VehiclePayload payload = new VehiclePayload(vehicleId, plateNumber, brand, model, fuelType, status, mileage);
		return buildEnvelope("VEHICLE_CREATED", payload);
	}

	public static KafkaEventEnvelope<VehiclePayload> vehicleUpdated(UUID vehicleId, String plateNumber, String brand, String model, String fuelType, String status, Integer mileage) {
		VehiclePayload payload = new VehiclePayload(vehicleId, plateNumber, brand, model, fuelType, status, mileage);
		return buildEnvelope("VEHICLE_UPDATED", payload);
	}

	public static KafkaEventEnvelope<VehiclePayload> vehicleStatusChanged(UUID vehicleId, String plateNumber, String oldStatus, String newStatus) {
		// Ici on utilise l'objet StatusChange pour respecter le contrat !
		VehiclePayload payload = new VehiclePayload(vehicleId, plateNumber, null, null, null, new StatusChange(oldStatus, newStatus), null);
		return buildEnvelope("VEHICLE_STATUS_CHANGED", payload);
	}

	public static KafkaEventEnvelope<VehiclePayload> vehicleDeleted(UUID vehicleId, String plateNumber) {
		VehiclePayload payload = new VehiclePayload(vehicleId, plateNumber, null, null, null, null, null);
		return buildEnvelope("VEHICLE_DELETED", payload);
	}

	// --- TOPIC 3 : ASSIGNATIONS ---

	public static KafkaEventEnvelope<AssignmentPayload> vehicleAssigned(UUID assignmentId, UUID vehicleId, UUID driverId, String notes) {
		AssignmentPayload payload = new AssignmentPayload(assignmentId, vehicleId, driverId, OffsetDateTime.now(), null, notes);
		return buildEnvelope("VEHICLE_ASSIGNED", payload);
	}

	public static KafkaEventEnvelope<AssignmentPayload> vehicleUnassigned(UUID assignmentId, UUID vehicleId, UUID driverId) {
		AssignmentPayload payload = new AssignmentPayload(assignmentId, vehicleId, driverId, null, OffsetDateTime.now(), null);
		return buildEnvelope("VEHICLE_UNASSIGNED", payload);
	}

	// --- REJECTION (Saga compensation) ---

	public static KafkaEventEnvelope<MaintenancePayload> maintenanceRejected(MaintenancePayload originalPayload) {
		return buildEnvelope("MAINTENANCE_REJECTED", originalPayload);
	}

	// --- MÉTHODE UTILITAIRE INTERNE ---

	private static <T> KafkaEventEnvelope<T> buildEnvelope(String eventType, T payload) {
		EventMetadata metadata = new EventMetadata(UUID.randomUUID(), "vehicle-service"); // On pourrait passer un dispatcherId ici plus tard
		return new KafkaEventEnvelope<>(
				UUID.randomUUID(),
				eventType,
				VERSION,
				OffsetDateTime.now(),
				payload,
				metadata
		);
	}
}
