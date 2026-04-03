package com.flotte.maintenance.events;

import com.flotte.maintenance.service.MaintenanceService;
import com.flotte.vehicle.events.KafkaEventEnvelope;
import com.flotte.vehicle.events.VehiclePayload;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
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
		VehiclePayload payload = new VehiclePayload(
				vehicleId, "AA-123-BB", "Renault", "Kangoo", "electric", "available", 15000
		);
		
		KafkaEventEnvelope<VehiclePayload> event = new KafkaEventEnvelope<>(
				UUID.randomUUID(),
				"VEHICLE_UPDATED",
				"1.0",
				OffsetDateTime.now(),
				payload,
				null
		);

		consumer.consumeVehicleEvent(event);

		verify(maintenanceService).processMileageUpdate(vehicleId, 15000);
	}
}
