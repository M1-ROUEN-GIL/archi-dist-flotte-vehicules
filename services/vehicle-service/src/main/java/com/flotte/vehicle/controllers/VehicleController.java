package com.flotte.vehicle.controllers;

import com.flotte.vehicle.dto.*;
import com.flotte.vehicle.models.enums.VehicleStatus;
import com.flotte.vehicle.services.VehicleService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping({"/vehicles", "/vehicles/"})
public class VehicleController {

	private final VehicleService service;

	// Injection par constructeur
	public VehicleController(VehicleService service) {
		this.service = service;
	}

	// 1. LISTER TOUS LES VÉHICULES
	@GetMapping
	public List<VehicleResponse> getAllVehicles(@RequestParam(required = false) VehicleStatus status) {
		return service.getAllVehicles(status);
	}

	// 2. RÉCUPÉRER UN VÉHICULE PAR ID
	@GetMapping("/{id}")
	public VehicleResponse getVehicleById(@PathVariable UUID id) {
		return service.getVehicleById(id);
	}

	// 3. CRÉER UN VÉHICULE
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	@PreAuthorize("hasRole('admin')")
	public VehicleResponse createVehicle(@Valid @RequestBody VehicleInput input) {
		return service.createVehicle(input);
	}

	// 4. METTRE À JOUR LES INFOS
	@PutMapping("/{id}")
	@PreAuthorize("hasRole('admin')")
	public VehicleResponse updateVehicle(
			@PathVariable UUID id,
			@Valid @RequestBody VehicleUpdate update) {
		return service.updateVehicle(id, update);
	}

	// 5. CHANGER LE STATUT
	@PatchMapping("/{id}/status")
	@PreAuthorize("hasAnyRole('admin', 'technician')")
	public VehicleResponse updateVehicleStatus(
			@PathVariable UUID id,
			@Valid @RequestBody VehicleStatusInput statusInput) {
		return service.updateVehicleStatus(id, statusInput.status());
	}

	// 6. SUPPRIMER UN VÉHICULE (Soft Delete)
	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@PreAuthorize("hasRole('admin')")
	public void deleteVehicle(@PathVariable UUID id) {
		service.deleteVehicle(id);
	}

	@GetMapping("/{id}/assignments")
	@PreAuthorize("hasAnyRole('admin', 'technician')")
	public List<AssignmentResponse> getAssignments(@PathVariable UUID id) {
		return service.getAssignments(id);
	}

	// POST /vehicles/{id}/assignments
	@PostMapping("/{id}/assignments")
	@ResponseStatus(HttpStatus.CREATED)
	@PreAuthorize("hasRole('admin')")
	public AssignmentResponse createAssignment(
			@PathVariable UUID id,
			@Valid @RequestBody AssignmentInput input) {
		return service.createAssignment(id, input);
	}

	// DELETE /vehicles/{id}/assignments/current
	@DeleteMapping("/{id}/assignments/current")
	@PreAuthorize("hasRole('admin')")
	public AssignmentResponse endCurrentAssignment(@PathVariable UUID id) {
		return service.endCurrentAssignment(id);
	}
}