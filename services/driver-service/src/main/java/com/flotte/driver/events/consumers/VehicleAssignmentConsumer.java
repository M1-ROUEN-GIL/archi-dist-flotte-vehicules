package com.flotte.driver.events.consumers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flotte.driver.dto.DriverStatusInput;
import com.flotte.driver.events.AssignmentPayload;
import com.flotte.driver.events.KafkaEventEnvelope;
import com.flotte.driver.models.enums.DriverStatus;
import com.flotte.driver.services.DriverService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class VehicleAssignmentConsumer {

    private static final Logger log = LoggerFactory.getLogger(VehicleAssignmentConsumer.class);
    private final ObjectMapper objectMapper;

    private final DriverService driverService;

    public VehicleAssignmentConsumer(ObjectMapper objectMapper, DriverService driverService) {
        this.objectMapper = objectMapper;
        this.driverService = driverService;
    }

    @KafkaListener(topics = "flotte.assignments.events", groupId = "driver-service-group")
    public void consumeAssignmentEvent(String message) {
        try {
            // Astuce d'expert : on reçoit un String brut et on utilise ObjectMapper
            // avec TypeReference pour gérer parfaitement l'Enveloppe Générique (KafkaEventEnvelope<T>)
            KafkaEventEnvelope<AssignmentPayload> event = objectMapper.readValue(
                    message,
                    new TypeReference<KafkaEventEnvelope<AssignmentPayload>>() {}
            );

            log.info("🎧 Événement d'assignation reçu : [{}] pour le chauffeur {}",
                    event.eventType(), event.payload().driverId());

            if ("VEHICLE_ASSIGNED".equals(event.eventType())) {
                log.info("-> 🚚 Le chauffeur {} part en tournée (Véhicule: {}) !",
                        event.payload().driverId(), event.payload().vehicleId());

                DriverStatusInput statusInput = new DriverStatusInput(DriverStatus.ON_TOUR);
                driverService.updateDriverStatus(event.payload().driverId(), statusInput);

            } else if ("VEHICLE_UNASSIGNED".equals(event.eventType())) {
                log.info("-> 🏠 Le chauffeur {} a fini sa tournée.", event.payload().driverId());

                DriverStatusInput statusInput = new DriverStatusInput(DriverStatus.ACTIVE);
                driverService.updateDriverStatus(event.payload().driverId(), statusInput);

            }

        } catch (Exception e) {
            log.error("❌ Erreur lors de la lecture du message Kafka", e);
        }
    }
}