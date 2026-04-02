package com.flotte.vehicle.events.producers;

import com.flotte.vehicle.events.VehicleEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class VehicleEventProducer {

    private static final Logger log = LoggerFactory.getLogger(VehicleEventProducer.class);
    private static final String TOPIC = "flotte.vehicules.events";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public VehicleEventProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publish(VehicleEvent event) {
        kafkaTemplate.send(TOPIC, event.vehicleId().toString(), event);
        log.info("Kafka event publié → type={} vehicleId={}", event.eventType(), event.vehicleId());
    }
}