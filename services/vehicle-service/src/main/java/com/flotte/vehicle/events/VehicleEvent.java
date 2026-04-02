package com.flotte.vehicle.events;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.OffsetDateTime;
import java.util.UUID;

public record VehicleEvent(
        String eventType,        // "vehicle.created", "vehicle.deleted"...
        UUID vehicleId,
        String plateNumber,
        String brand,
        String model,
        String status,
        UUID driverId,           // null sauf pour assigned/unassigned
        Integer mileageKm,

        // On force Jackson à écrire la date sous forme de texte (ISO-8601)
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        OffsetDateTime occurredAt
) {
    // Factories — une méthode par type d'événement

    public static VehicleEvent created(UUID id, String plateNumber, String brand, String model, String status, Integer mileageKm) {
        return new VehicleEvent("vehicle.created", id, plateNumber, brand, model, status, null, mileageKm, OffsetDateTime.now());
    }

    public static VehicleEvent updated(UUID id, String plateNumber, Integer mileageKm) {
        return new VehicleEvent("vehicle.updated", id, plateNumber, null, null, null, null, mileageKm, OffsetDateTime.now());
    }

    public static VehicleEvent deleted(UUID id, String plateNumber) {
        return new VehicleEvent("vehicle.deleted", id, plateNumber, null, null, null, null, null, OffsetDateTime.now());
    }

    public static VehicleEvent statusChanged(UUID id, String plateNumber, String newStatus) {
        return new VehicleEvent("vehicle.status.changed", id, plateNumber, null, null, newStatus, null, null, OffsetDateTime.now());
    }

    public static VehicleEvent assigned(UUID id, String plateNumber, UUID driverId) {
        return new VehicleEvent("vehicle.assigned", id, plateNumber, null, null, null, driverId, null, OffsetDateTime.now());
    }

    public static VehicleEvent unassigned(UUID id, String plateNumber, UUID driverId) {
        return new VehicleEvent("vehicle.unassigned", id, plateNumber, null, null, null, driverId, null, OffsetDateTime.now());
    }
}