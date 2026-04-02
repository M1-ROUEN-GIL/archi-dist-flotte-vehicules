package com.flotte.vehicle.events.consumers;

import com.flotte.vehicle.events.MaintenanceEvent;
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
    public void consume(MaintenanceEvent event) {
        log.info("Événement de maintenance reçu : type={} vehicleId={}", event.eventType(), event.vehicleId());

        try {
            switch (event.eventType()) {
                case "maintenance.started":
                    log.info("Mise à jour du statut du véhicule {} en IN_MAINTENANCE", event.vehicleId());
                    vehicleService.updateVehicleStatus(event.vehicleId(), VehicleStatus.in_maintenance);
                    break;
                case "maintenance.completed":
                    log.info("Mise à jour du statut du véhicule {} en AVAILABLE", event.vehicleId());
                    vehicleService.updateVehicleStatus(event.vehicleId(), VehicleStatus.available);
                    break;
                default:
                    log.debug("Type d'événement ignoré par le service véhicule : {}", event.eventType());
            }
        } catch (Exception e) {
            log.error("Erreur lors du traitement de l'événement de maintenance pour le véhicule {}", event.vehicleId(), e);
        }
    }
}
