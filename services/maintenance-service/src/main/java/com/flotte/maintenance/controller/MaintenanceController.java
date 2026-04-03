package com.flotte.maintenance.controller;

import com.flotte.maintenance.dto.MaintenanceCreateRequest;
import com.flotte.maintenance.dto.MaintenanceStatusUpdate;
import com.flotte.maintenance.dto.MaintenanceUpdateRequest;
import com.flotte.maintenance.model.MaintenancePriority;
import com.flotte.maintenance.model.MaintenanceRecord;
import com.flotte.maintenance.model.MaintenanceStatus;
import com.flotte.maintenance.service.MaintenanceService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping({"/maintenance", "/maintenance/"})
public class MaintenanceController {

	private final MaintenanceService service;

	public MaintenanceController(MaintenanceService service) {
		this.service = service;
	}

	@PostMapping
	@PreAuthorize("hasRole('admin')")
	public ResponseEntity<MaintenanceRecord> createRecord(@RequestBody MaintenanceCreateRequest request) {
		return new ResponseEntity<>(service.createRecord(request), HttpStatus.CREATED);
	}

	@GetMapping
	@PreAuthorize("hasAnyRole('admin', 'technician')")
	public ResponseEntity<List<MaintenanceRecord>> getAllRecords(
			@RequestParam(required = false) UUID vehicleId,
			@RequestParam(required = false) MaintenanceStatus status,
			@RequestParam(required = false) MaintenancePriority priority) {
		return ResponseEntity.ok(service.getAllRecords(vehicleId, status, priority));
	}

	@GetMapping("/vehicle/{vehicleId}")
	@PreAuthorize("hasAnyRole('admin', 'technician')")
	public ResponseEntity<List<MaintenanceRecord>> getVehicleHistory(@PathVariable UUID vehicleId) {
		return ResponseEntity.ok(service.getVehicleHistory(vehicleId));
	}

	@GetMapping("/{id}")
	@PreAuthorize("hasAnyRole('admin', 'technician')")
	public ResponseEntity<MaintenanceRecord> getRecordById(@PathVariable UUID id) {
		return ResponseEntity.ok(service.getRecordById(id));
	}

	@PutMapping("/{id}")
	@PreAuthorize("hasAnyRole('admin', 'technician')")
	public ResponseEntity<MaintenanceRecord> updateRecord(@PathVariable UUID id, @RequestBody MaintenanceUpdateRequest request) {
		return ResponseEntity.ok(service.updateRecord(id, request));
	}

	@PatchMapping("/{id}/status")
	@PreAuthorize("hasAnyRole('admin', 'technician')")
	public ResponseEntity<MaintenanceRecord> updateStatus(@PathVariable UUID id, @RequestBody MaintenanceStatusUpdate update) {
		return ResponseEntity.ok(service.updateStatus(id, update));
	}
}
