package com.flotte.maintenance.events;

import com.flotte.vehicle.events.KafkaEventEnvelope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class MaintenanceEventProducer {
	private static final Logger logger = LoggerFactory.getLogger(MaintenanceEventProducer.class);
	private static final String ALERTS_TOPIC = "flotte.alertes.events";
	private static final String MAINTENANCE_TOPIC = "flotte.maintenance.events";

	private final KafkaTemplate<String, Object> kafkaTemplate;

	public MaintenanceEventProducer(KafkaTemplate<String, Object> kafkaTemplate) {
		this.kafkaTemplate = kafkaTemplate;
	}

	public void publishAlert(AlertEvent event) {
		logger.info("Publishing alert: {}", event);
		kafkaTemplate.send(ALERTS_TOPIC, event.vehicleId().toString(), event);
	}

	public void publishMaintenanceEvent(KafkaEventEnvelope<MaintenancePayload> event) {
		String key = event.payload().vehicleId().toString();
		logger.info("Publishing maintenance event {} for vehicle {}: {}", event.eventType(), key, event);
		kafkaTemplate.send(MAINTENANCE_TOPIC, key, event);
	}
}
