package com.flotte.vehicle.events.producers;

import com.flotte.vehicle.events.AssignmentPayload;
import com.flotte.vehicle.events.KafkaEventEnvelope;
import com.flotte.vehicle.events.VehiclePayload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class VehicleEventProducer {

	private static final Logger log = LoggerFactory.getLogger(VehicleEventProducer.class);

	// Définition des DEUX topics selon le contrat
	private static final String VEHICLE_TOPIC = "flotte.vehicules.events";
	private static final String ASSIGNMENT_TOPIC = "flotte.assignments.events";
	private static final String MAINTENANCE_TOPIC = "flotte.maintenance.events";

	private final KafkaTemplate<String, Object> kafkaTemplate;

	public VehicleEventProducer(KafkaTemplate<String, Object> kafkaTemplate) {
		this.kafkaTemplate = kafkaTemplate;
	}

	// 1. Publication pour le cycle de vie du véhicule
	public void publishVehicleEvent(KafkaEventEnvelope<VehiclePayload> event) {
		String key = event.payload().vehicleId().toString();
		kafkaTemplate.send(VEHICLE_TOPIC, key, event);
		log.info("Kafka event publié sur {} → type={} vehicleId={}", VEHICLE_TOPIC, event.eventType(), key);
	}

	// 2. Publication pour les assignations
	public void publishAssignmentEvent(KafkaEventEnvelope<AssignmentPayload> event) {
		String key = event.payload().vehicleId().toString(); // La clé reste le vehicleId pour garantir l'ordre !
		kafkaTemplate.send(ASSIGNMENT_TOPIC, key, event);
		log.info("Kafka event publié sur {} → type={} vehicleId={} driverId={}",
				ASSIGNMENT_TOPIC, event.eventType(), key, event.payload().driverId());
	}

	public void publishMaintenanceEvent(KafkaEventEnvelope<?> event) {
		kafkaTemplate.send(MAINTENANCE_TOPIC, event);
		log.info("Kafka event publié sur {} → type={}", MAINTENANCE_TOPIC, event.eventType());
	}
}
