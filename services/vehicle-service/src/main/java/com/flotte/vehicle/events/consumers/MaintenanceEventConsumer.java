package com.flotte.vehicle.events.consumers;

import com.flotte.maintenance.events.MaintenancePayload;
import com.flotte.vehicle.events.KafkaEventEnvelope;
import com.flotte.vehicle.events.VehicleEventFactory;
import com.flotte.vehicle.events.producers.VehicleEventProducer;
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
	private final VehicleEventProducer eventProducer;

	public MaintenanceEventConsumer(VehicleService vehicleService, VehicleEventProducer eventProducer) {
		this.vehicleService = vehicleService;
		this.eventProducer = eventProducer;
	}

	@KafkaListener(topics = "flotte.maintenance.events", groupId = "vehicle-maintenance-group")
	public void consume(KafkaEventEnvelope<MaintenancePayload> event) {
		log.info("Événement de maintenance reçu : type={} vehicleId={} statusChange={}", 
				event.eventType(), event.payload().vehicleId(), event.payload().status());

		try {
			switch (event.eventType()) {
				case "MAINTENANCE_STARTED":
					updateVehicleStatus(event.payload().vehicleId(), VehicleStatus.IN_MAINTENANCE);
					break;
				case "MAINTENANCE_COMPLETED":
					updateVehicleStatus(event.payload().vehicleId(), VehicleStatus.AVAILABLE);
					break;
				case "MAINTENANCE_CANCELLED":
					updateVehicleStatus(event.payload().vehicleId(), VehicleStatus.AVAILABLE);
					break;
				case "MAINTENANCE_UPDATED":
					handleStatusUpdate(event.payload());
					break;
				case "MAINTENANCE_REJECTED":
					log.debug("MAINTENANCE_REJECTED ignoré par le consommateur (boucle possible)");
					break;
				default:
					log.debug("Type d'événement ignoré par le service véhicule : {}", event.eventType());
			}
		} catch (Exception e) {
			log.error("Erreur lors du traitement de l'événement de maintenance pour le véhicule {}. Déclenchement de la compensation.", event.payload().vehicleId(), e);
			eventProducer.publishMaintenanceEvent(VehicleEventFactory.maintenanceRejected(event.payload()));
		}
	}

	private void handleStatusUpdate(MaintenancePayload payload) {
		String newStatus = payload.status().current();
		log.info("Traitement d'une mise à jour de maintenance pour le véhicule {} (nouveau statut={})", 
				payload.vehicleId(), newStatus);
		
		if ("IN_PROGRESS".equals(newStatus)) {
			updateVehicleStatus(payload.vehicleId(), VehicleStatus.IN_MAINTENANCE);
		} else if ("COMPLETED".equals(newStatus) || "CANCELLED".equals(newStatus)) {
			updateVehicleStatus(payload.vehicleId(), VehicleStatus.AVAILABLE);
		}
	}

	private void updateVehicleStatus(java.util.UUID vehicleId, VehicleStatus status) {
		log.info("Mise à jour du statut du véhicule {} en {}", vehicleId, status);
		vehicleService.updateVehicleStatus(vehicleId, status);
	}
}
