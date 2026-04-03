package com.flotte.maintenance.service;

import com.flotte.maintenance.dto.MaintenanceCreateRequest;
import com.flotte.maintenance.dto.MaintenanceStatusUpdate;
import com.flotte.maintenance.dto.MaintenanceUpdateRequest;
import com.flotte.maintenance.events.AlertEvent;
import com.flotte.maintenance.events.MaintenanceEventFactory;
import com.flotte.maintenance.events.MaintenanceEventProducer;
import com.flotte.maintenance.model.MaintenanceRecord;
import com.flotte.maintenance.model.MaintenanceStatus;
import com.flotte.maintenance.repository.MaintenanceRepository;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class MaintenanceService {

	private final MaintenanceRepository repository;
	private final MaintenanceEventProducer eventProducer;

	public MaintenanceService(MaintenanceRepository repository, MaintenanceEventProducer eventProducer) {
		this.repository = repository;
		this.eventProducer = eventProducer;
	}

	@Transactional
	public MaintenanceRecord createRecord(MaintenanceCreateRequest request) {
		MaintenanceRecord record = new MaintenanceRecord();
		record.setVehicleId(request.vehicleId());
		record.setType(request.type());
		record.setPriority(request.priority());
		record.setScheduledDate(request.scheduledDate());
		record.setDescription(request.description());
		record.setStatus(MaintenanceStatus.SCHEDULED);
		MaintenanceRecord savedRecord = repository.save(record);
		
		eventProducer.publishMaintenanceEvent(MaintenanceEventFactory.scheduled(savedRecord));
			
		return savedRecord;
	}

	public List<MaintenanceRecord> getVehicleHistory(UUID vehicleId) {
		return repository.findByVehicleId(vehicleId);
	}

	public MaintenanceRecord getRecordById(UUID id) {
		return repository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Record not found"));
	}

	@Transactional
	public MaintenanceRecord updateRecord(UUID id, MaintenanceUpdateRequest request) {
		MaintenanceRecord record = getRecordById(id);
		MaintenanceStatus oldStatus = record.getStatus();

		if (request.description() != null) record.setDescription(request.description());
		if (request.priority() != null) record.setPriority(request.priority());
		if (request.scheduledDate() != null) record.setScheduledDate(request.scheduledDate());
		
		if (request.status() != null && request.status() != oldStatus) {
			record.setStatus(request.status());
			publishStatusChangeEvent(record, oldStatus);
		} else {
			MaintenanceRecord updatedRecord = repository.save(record);
			eventProducer.publishMaintenanceEvent(MaintenanceEventFactory.updated(updatedRecord, oldStatus));
			return updatedRecord;
		}
		
		return repository.save(record);
	}

	@Transactional
	public MaintenanceRecord updateStatus(UUID id, MaintenanceStatusUpdate update) {
		MaintenanceRecord record = getRecordById(id);
		MaintenanceStatus oldStatus = record.getStatus();
		
		record.setStatus(update.status());
		if (update.costEur() != null) record.setCostEur(update.costEur());
		if (update.notes() != null) record.setNotes(update.notes());
		if (update.mileageAtService() != null) record.setMileageAtService(update.mileageAtService());
		if (update.nextServiceKm() != null) record.setNextServiceKm(update.nextServiceKm());
		
		publishStatusChangeEvent(record, oldStatus);
		
		return repository.save(record);
	}

	private void publishStatusChangeEvent(MaintenanceRecord record, MaintenanceStatus oldStatus) {
		if (record.getStatus() == MaintenanceStatus.COMPLETED) {
			record.setCompletedDate(LocalDate.now());
			eventProducer.publishMaintenanceEvent(MaintenanceEventFactory.completed(record, oldStatus));
		} else if (record.getStatus() == MaintenanceStatus.IN_PROGRESS && oldStatus != MaintenanceStatus.IN_PROGRESS) {
			eventProducer.publishMaintenanceEvent(MaintenanceEventFactory.started(record, oldStatus));
		} else if (record.getStatus() == MaintenanceStatus.CANCELLED && oldStatus != MaintenanceStatus.CANCELLED) {
			eventProducer.publishMaintenanceEvent(MaintenanceEventFactory.cancelled(record, oldStatus));
		} else {
			eventProducer.publishMaintenanceEvent(MaintenanceEventFactory.updated(record, oldStatus));
		}
	}

	// Task to mark overdue maintenance
	@Scheduled(cron = "0 0 1 * * ?") // Every day at 1 AM
	@Transactional
	public void checkOverdueMaintenance() {
		List<MaintenanceRecord> scheduledRecords = repository.findByStatus(MaintenanceStatus.SCHEDULED);
		LocalDate today = LocalDate.now();
		for (MaintenanceRecord record : scheduledRecords) {
			if (record.getScheduledDate().isBefore(today)) {
				record.setStatus(MaintenanceStatus.OVERDUE);
				repository.save(record);
				
				eventProducer.publishAlert(AlertEvent.overdue(
					record.getVehicleId(),
					"Maintenance " + record.getType() + " is overdue since " + record.getScheduledDate()
				));
			}
		}
	}

	// Preventive alerts logic
	public void processMileageUpdate(UUID vehicleId, int currentMileage) {
		// Find last completed maintenance for this vehicle
		List<MaintenanceRecord> records = repository.findByVehicleId(vehicleId);
		records.stream()
			.filter(r -> r.getStatus() == MaintenanceStatus.COMPLETED && r.getNextServiceKm() != null)
			.sorted((r1, r2) -> r2.getCompletedDate().compareTo(r1.getCompletedDate()))
			.findFirst()
			.ifPresent(lastRecord -> {
				if (currentMileage >= lastRecord.getNextServiceKm() - 500) { // Alert 500km before
					eventProducer.publishAlert(AlertEvent.preventive(
						vehicleId,
						"Vehicle mileage is " + currentMileage + " km. Maintenance recommended at " + lastRecord.getNextServiceKm() + " km."
					));
				}
			});
	}
}
