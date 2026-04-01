package com.flotte.maintenance.events;

import com.flotte.maintenance.service.MaintenanceService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MaintenanceEventConsumerTest {

    @Mock
    private MaintenanceService maintenanceService;

    @InjectMocks
    private MaintenanceEventConsumer consumer;

    @Test
    void consumeVehicleEvent_ShouldCallProcessMileageUpdate() {
        UUID vehicleId = UUID.randomUUID();
        VehicleEvent event = new VehicleEvent("vehicle.mileage.updated", vehicleId, 15000);

        consumer.consumeVehicleEvent(event);

        verify(maintenanceService).processMileageUpdate(vehicleId, 15000);
    }
}
