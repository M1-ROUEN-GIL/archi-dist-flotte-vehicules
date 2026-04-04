package com.flotte.driver.controllers;

import com.flotte.driver.dto.*;
import com.flotte.driver.models.enums.DriverStatus;
import com.flotte.driver.services.DriverService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping({"/drivers", "/drivers/"})
public class DriverController {
	private final DriverService driverService;

	public DriverController(DriverService driverService) {
		this.driverService = driverService;
	}

	// ==========================================
	// ROUTES CONDUCTEURS (DRIVERS)
	// ==========================================

	@GetMapping
	public List<DriverResponse> getDrivers(@RequestParam (required = false)DriverStatus status){
		return driverService.getAllDrivers(status);
	}

	@GetMapping("/{id}")
	public DriverResponse getDriverById(@PathVariable UUID id){
		return driverService.getDriverById(id);
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	@PreAuthorize("hasAnyRole('admin', 'manager')")
	public DriverResponse createDriver(@Valid @RequestBody DriverInput request){
		return driverService.createDriver(request);
	}

	@PutMapping("/{id}")
	@PreAuthorize("hasAnyRole('admin', 'manager')")
	public DriverResponse updateDriver(@PathVariable UUID id, @Valid @RequestBody DriverUpdate request){
		return driverService.updateDriver(id, request);
	}

	@PatchMapping("/{id}/status")
	@PreAuthorize("hasAnyRole('admin', 'manager')")
	public DriverResponse updateDriverStatus(@PathVariable UUID id,
											 @Valid @RequestBody DriverStatusInput request){
		return driverService.updateDriverStatus(id, request);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT) // Retourne 204 No Content pour dire "c'est supprimé, rien à afficher"
	@PreAuthorize("hasRole('admin')")
	public void deleteDriver(@PathVariable UUID id){
		driverService.deleteDriver(id);
	}

	// ==========================================
	// ROUTES PERMIS (LICENSES)
	// ==========================================

	@GetMapping("/{id}/licenses")
	public List<LicenseResponse> getDriverLicenses(@PathVariable UUID id){
		return driverService.getDriverLicenses(id);
	}

	@PostMapping("/{id}/licenses")
	@ResponseStatus(HttpStatus.CREATED)
	@PreAuthorize("hasAnyRole('admin', 'manager')")
	public LicenseResponse addLicenseToDriver(@PathVariable UUID id,
											  @Valid @RequestBody LicenseInput request){
		return driverService.addLicenseToDriver(id, request);
	}
}
