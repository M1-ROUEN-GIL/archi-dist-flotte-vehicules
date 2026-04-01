package com.flotte.maintenance.events;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class MaintenanceEventProducer {
    private static final Logger logger = LoggerFactory.getLogger(MaintenanceEventProducer.class);
    private static final String ALERTS_TOPIC = "alerts-topic";
    private static final String MAINTENANCE_TOPIC = "maintenance-topic";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public MaintenanceEventProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishAlert(AlertEvent event) {
        logger.info("Publishing alert: {}", event);
        kafkaTemplate.send(ALERTS_TOPIC, event.vehicleId().toString(), event);
    }

    public void publishMaintenanceEvent(MaintenanceEvent event) {
        logger.info("Publishing maintenance event: {}", event);
        kafkaTemplate.send(MAINTENANCE_TOPIC, event.vehicleId().toString(), event);
    }
}
