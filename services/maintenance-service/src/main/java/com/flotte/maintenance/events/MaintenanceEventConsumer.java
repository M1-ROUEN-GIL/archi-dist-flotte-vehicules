package com.flotte.maintenance.events;

import com.flotte.maintenance.service.MaintenanceService;
import com.flotte.vehicle.events.KafkaEventEnvelope;
import com.flotte.vehicle.events.VehiclePayload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class MaintenanceEventConsumer {
	private static final Logger logger = LoggerFactory.getLogger(MaintenanceEventConsumer.class);

	private final MaintenanceService maintenanceService;

	public MaintenanceEventConsumer(MaintenanceService maintenanceService) {
		this.maintenanceService = maintenanceService;
	}

	@KafkaListener(topics = "flotte.vehicules.events", groupId = "maintenance-group")
	public void consumeVehicleEvent(KafkaEventEnvelope<VehiclePayload> event) {
		logger.info("Received vehicle event type {}: {}", event.eventType(), event.payload());
		
		VehiclePayload payload = event.payload();
		if (payload != null && payload.vehicleId() != null && payload.mileageKm() != null) {
			maintenanceService.processMileageUpdate(payload.vehicleId(), payload.mileageKm());
		}
	}

	@KafkaListener(topics = "flotte.maintenance.events", groupId = "maintenance-compensation-group")
	public void consumeMaintenanceEvent(KafkaEventEnvelope<MaintenancePayload> event) {
		if ("MAINTENANCE_REJECTED".equals(event.eventType())) {
			logger.warn("REÇU MAINTENANCE_REJECTED pour record {}. Annulation en cours (Saga compensation).", 
					event.payload().recordId());
			maintenanceService.cancelRecord(event.payload().recordId(), "Le service véhicule a rejeté la mise à jour.");
		}
	}
}
