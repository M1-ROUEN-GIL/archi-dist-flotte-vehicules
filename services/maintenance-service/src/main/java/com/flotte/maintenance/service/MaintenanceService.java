package com.flotte.maintenance.service;

import com.flotte.maintenance.dto.MaintenanceCreateRequest;
import com.flotte.maintenance.dto.MaintenanceStatusUpdate;
import com.flotte.maintenance.events.AlertEvent;
import com.flotte.maintenance.events.MaintenanceEvent;
import com.flotte.maintenance.events.MaintenanceEventProducer;
import com.flotte.maintenance.model.MaintenanceRecord;
import com.flotte.maintenance.model.MaintenanceStatus;
import com.flotte.maintenance.repository.MaintenanceRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        
        eventProducer.publishMaintenanceEvent(MaintenanceEvent.scheduled(
            savedRecord.getId(), savedRecord.getVehicleId(), savedRecord.getType()));
            
        return savedRecord;
    }

    public List<MaintenanceRecord> getVehicleHistory(UUID vehicleId) {
        return repository.findByVehicleId(vehicleId);
    }

    public MaintenanceRecord getRecordById(UUID id) {
        return repository.findById(id).orElseThrow(() -> new RuntimeException("Record not found"));
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
        
        if (update.status() == MaintenanceStatus.COMPLETED) {
            record.setCompletedDate(LocalDate.now());
            eventProducer.publishMaintenanceEvent(MaintenanceEvent.completed(
                record.getId(), record.getVehicleId(), record.getType()));
        } else if (update.status() == MaintenanceStatus.IN_PROGRESS && oldStatus != MaintenanceStatus.IN_PROGRESS) {
            eventProducer.publishMaintenanceEvent(MaintenanceEvent.started(
                record.getId(), record.getVehicleId(), record.getType()));
        } else if (update.status() == MaintenanceStatus.CANCELLED && oldStatus != MaintenanceStatus.CANCELLED) {
            eventProducer.publishMaintenanceEvent(MaintenanceEvent.cancelled(
                record.getId(), record.getVehicleId(), record.getType()));
        }
        
        return repository.save(record);
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
