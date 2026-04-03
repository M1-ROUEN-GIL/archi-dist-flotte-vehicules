package com.flotte.maintenance.service;

import com.flotte.maintenance.dto.MaintenanceCreateRequest;
import com.flotte.maintenance.dto.MaintenanceStatusUpdate;
import com.flotte.maintenance.events.MaintenanceEventProducer;
import com.flotte.maintenance.model.MaintenancePriority;
import com.flotte.maintenance.model.MaintenanceRecord;
import com.flotte.maintenance.model.MaintenanceStatus;
import com.flotte.maintenance.model.MaintenanceType;
import com.flotte.maintenance.repository.MaintenanceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MaintenanceServiceTest {

	@Mock
	private MaintenanceRepository repository;

	@Mock
	private MaintenanceEventProducer eventProducer;

	@InjectMocks
	private MaintenanceService service;

	private UUID vehicleId;
	private MaintenanceRecord record;

	@BeforeEach
	void setUp() {
		vehicleId = UUID.randomUUID();
		record = new MaintenanceRecord();
		record.setId(UUID.randomUUID());
		record.setVehicleId(vehicleId);
		record.setType(MaintenanceType.PREVENTIVE);
		record.setStatus(MaintenanceStatus.SCHEDULED);
		record.setScheduledDate(LocalDate.now().plusDays(7));
	}

	@Test
	void createRecord_ShouldSaveAndPublishEvent() {
		MaintenanceCreateRequest request = new MaintenanceCreateRequest(
				vehicleId, MaintenanceType.PREVENTIVE, MaintenancePriority.MEDIUM,
				LocalDate.now().plusDays(7), "Description");

		when(repository.save(any(MaintenanceRecord.class))).thenReturn(record);

		MaintenanceRecord saved = service.createRecord(request);

		assertNotNull(saved);
		assertEquals(vehicleId, saved.getVehicleId());
		verify(repository).save(any(MaintenanceRecord.class));
		verify(eventProducer).publishMaintenanceEvent(any());
	}

	@Test
	void updateStatus_ToCompleted_ShouldSetDateAndPublishEvent() {
		MaintenanceStatusUpdate update = new MaintenanceStatusUpdate(
				MaintenanceStatus.COMPLETED, null, "Notes", 10000, 20000);

		when(repository.findById(record.getId())).thenReturn(Optional.of(record));
		when(repository.save(any())).thenReturn(record);

		MaintenanceRecord updated = service.updateStatus(record.getId(), update);

		assertEquals(MaintenanceStatus.COMPLETED, updated.getStatus());
		assertNotNull(updated.getCompletedDate());
		verify(eventProducer).publishMaintenanceEvent(any());
	}

	@Test
	void checkOverdueMaintenance_ShouldMarkOverdueAndAlert() {
		record.setScheduledDate(LocalDate.now().minusDays(1));
		when(repository.findByStatus(MaintenanceStatus.SCHEDULED)).thenReturn(List.of(record));

		service.checkOverdueMaintenance();

		assertEquals(MaintenanceStatus.OVERDUE, record.getStatus());
		verify(eventProducer).publishAlert(any());
	}

	@Test
	void updateStatus_ToInProgress_ShouldPublishStartedEvent() {
		MaintenanceStatusUpdate update = new MaintenanceStatusUpdate(
				MaintenanceStatus.IN_PROGRESS, null, null, null, null);

		when(repository.findById(record.getId())).thenReturn(Optional.of(record));
		when(repository.save(any())).thenReturn(record);

		service.updateStatus(record.getId(), update);

		verify(eventProducer).publishMaintenanceEvent(argThat(e -> e.eventType().equals("MAINTENANCE_STARTED")));
	}

	@Test
	void updateStatus_ToCancelled_ShouldPublishCancelledEvent() {
		MaintenanceStatusUpdate update = new MaintenanceStatusUpdate(
				MaintenanceStatus.CANCELLED, null, null, null, null);

		when(repository.findById(record.getId())).thenReturn(Optional.of(record));
		when(repository.save(any())).thenReturn(record);

		service.updateStatus(record.getId(), update);

		verify(eventProducer).publishMaintenanceEvent(argThat(e -> e.eventType().equals("MAINTENANCE_CANCELLED")));
	}

	@Test
	void getVehicleHistory_ShouldReturnList() {
		when(repository.findByVehicleId(vehicleId)).thenReturn(List.of(record));
		List<MaintenanceRecord> history = service.getVehicleHistory(vehicleId);
		assertFalse(history.isEmpty());
		assertEquals(1, history.size());
	}

	@Test
	void getRecordById_ShouldReturnRecord() {
		when(repository.findById(record.getId())).thenReturn(Optional.of(record));
		MaintenanceRecord found = service.getRecordById(record.getId());
		assertNotNull(found);
		assertEquals(record.getId(), found.getId());
	}

	@Test
	void updateRecord_ShouldUpdateAndPublishEvent() {
		com.flotte.maintenance.dto.MaintenanceUpdateRequest update = new com.flotte.maintenance.dto.MaintenanceUpdateRequest(
				"New description", MaintenancePriority.HIGH, LocalDate.now().plusDays(5), MaintenanceStatus.IN_PROGRESS);
		
		when(repository.findById(record.getId())).thenReturn(Optional.of(record));
		when(repository.save(any())).thenReturn(record);

		service.updateRecord(record.getId(), update);

		assertEquals("New description", record.getDescription());
		assertEquals(MaintenanceStatus.IN_PROGRESS, record.getStatus());
		verify(eventProducer).publishMaintenanceEvent(any());
	}

	@Test
	void cancelRecord_ShouldChangeStatus() {
		when(repository.findById(record.getId())).thenReturn(Optional.of(record));
		when(repository.save(any())).thenReturn(record);

		service.cancelRecord(record.getId(), "Reason");

		assertEquals(MaintenanceStatus.CANCELLED, record.getStatus());
		verify(eventProducer).publishMaintenanceEvent(any());
	}

	@Test
	void processMileageUpdate_ShouldPublishAlertIfThresholdReached() {
		record.setStatus(MaintenanceStatus.COMPLETED);
		record.setCompletedDate(LocalDate.now());
		record.setNextServiceKm(15000);
		
		when(repository.findByVehicleId(vehicleId)).thenReturn(List.of(record));

		service.processMileageUpdate(vehicleId, 14600); // 400km before 15000

		verify(eventProducer).publishAlert(any());
	}
}
