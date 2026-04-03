package com.flotte.vehicle.events.consumers;

import com.flotte.maintenance.events.MaintenancePayload;
import com.flotte.vehicle.events.KafkaEventEnvelope;
import com.flotte.vehicle.models.enums.VehicleStatus;
import com.flotte.vehicle.services.VehicleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class MaintenanceEventConsumer {

	private static final Logger log = LoggerFactory.getLogger(MaintenanceEventConsumer.class);
	private final VehicleService vehicleService;

	public MaintenanceEventConsumer(VehicleService vehicleService) {
		this.vehicleService = vehicleService;
	}

	@KafkaListener(topics = "flotte.maintenance.events", groupId = "vehicle-maintenance-group")
	public void consume(KafkaEventEnvelope<MaintenancePayload> event) {
		log.info("Événement de maintenance reçu : type={} vehicleId={} statusChange={}", 
				event.eventType(), event.payload().vehicleId(), event.payload().status());

		try {
			switch (event.eventType()) {
				case "MAINTENANCE_STARTED":
					updateVehicleStatus(event.payload().vehicleId(), VehicleStatus.in_maintenance);
					break;
				case "MAINTENANCE_COMPLETED":
					updateVehicleStatus(event.payload().vehicleId(), VehicleStatus.available);
					break;
				case "MAINTENANCE_CANCELLED":
					updateVehicleStatus(event.payload().vehicleId(), VehicleStatus.available);
					break;
				case "MAINTENANCE_UPDATED":
					handleStatusUpdate(event.payload());
					break;
				default:
					log.debug("Type d'événement ignoré par le service véhicule : {}", event.eventType());
			}
		} catch (Exception e) {
			log.error("Erreur lors du traitement de l'événement de maintenance pour le véhicule {}", event.payload().vehicleId(), e);
		}
	}

	private void handleStatusUpdate(MaintenancePayload payload) {
		String newStatus = payload.status().current();
		log.info("Traitement d'une mise à jour de maintenance pour le véhicule {} (nouveau statut={})", 
				payload.vehicleId(), newStatus);
		
		if ("IN_PROGRESS".equals(newStatus)) {
			updateVehicleStatus(payload.vehicleId(), VehicleStatus.in_maintenance);
		} else if ("COMPLETED".equals(newStatus) || "CANCELLED".equals(newStatus)) {
			updateVehicleStatus(payload.vehicleId(), VehicleStatus.available);
		}
	}

	private void updateVehicleStatus(java.util.UUID vehicleId, VehicleStatus status) {
		log.info("Mise à jour du statut du véhicule {} en {}", vehicleId, status);
		vehicleService.updateVehicleStatus(vehicleId, status);
	}
}
