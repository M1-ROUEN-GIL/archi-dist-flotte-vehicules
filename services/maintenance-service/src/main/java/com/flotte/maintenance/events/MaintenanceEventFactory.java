package com.flotte.maintenance.events;

import com.flotte.maintenance.model.MaintenanceRecord;
import com.flotte.maintenance.model.MaintenanceStatus;
import com.flotte.vehicle.events.EventMetadata;
import com.flotte.vehicle.events.KafkaEventEnvelope;
import com.flotte.vehicle.events.StatusChange;

import java.time.OffsetDateTime;
import java.util.UUID;

public class MaintenanceEventFactory {

	private static final String VERSION = "1.0";

	public static KafkaEventEnvelope<MaintenancePayload> createEvent(String eventType, MaintenanceRecord record, MaintenanceStatus previousStatus) {
		MaintenancePayload payload = new MaintenancePayload(
				record.getId(),
				record.getVehicleId(),
				record.getType(),
				new StatusChange(
						previousStatus != null ? previousStatus.name() : null,
						record.getStatus().name()
				),
				record.getTechnicianId(),
				record.getCompletedDate(),
				record.getCostEur(),
				record.getNextServiceKm()
		);

		EventMetadata metadata = new EventMetadata(
				UUID.randomUUID(),
				"maintenance-service"
		);

		return new KafkaEventEnvelope<>(
				UUID.randomUUID(),
				eventType,
				VERSION,
				OffsetDateTime.now(),
				payload,
				metadata
		);
	}

	public static KafkaEventEnvelope<MaintenancePayload> scheduled(MaintenanceRecord record) {
		return createEvent("MAINTENANCE_SCHEDULED", record, null);
	}

	public static KafkaEventEnvelope<MaintenancePayload> started(MaintenanceRecord record, MaintenanceStatus previous) {
		return createEvent("MAINTENANCE_STARTED", record, previous);
	}

	public static KafkaEventEnvelope<MaintenancePayload> completed(MaintenanceRecord record, MaintenanceStatus previous) {
		return createEvent("MAINTENANCE_COMPLETED", record, previous);
	}

	public static KafkaEventEnvelope<MaintenancePayload> cancelled(MaintenanceRecord record, MaintenanceStatus previous) {
		return createEvent("MAINTENANCE_CANCELLED", record, previous);
	}

	public static KafkaEventEnvelope<MaintenancePayload> updated(MaintenanceRecord record, MaintenanceStatus previous) {
		return createEvent("MAINTENANCE_UPDATED", record, previous);
	}
}
