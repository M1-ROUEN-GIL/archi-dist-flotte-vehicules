package com.flotte.maintenance.events;

import com.flotte.maintenance.service.MaintenanceService;
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
    public void consumeVehicleEvent(VehicleEvent event) {
        logger.info("Received vehicle event: {}", event);
        if (event.mileageKm() != null) {
            maintenanceService.processMileageUpdate(event.vehicleId(), event.mileageKm());
        }
    }
}
