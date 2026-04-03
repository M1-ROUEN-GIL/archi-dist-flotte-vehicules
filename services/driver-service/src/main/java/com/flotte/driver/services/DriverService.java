package com.flotte.driver.services;

import com.flotte.driver.dto.*;
import com.flotte.driver.events.DriverEventFactory;
import com.flotte.driver.events.producers.DriverEventProducer;
import com.flotte.driver.models.Driver;
import com.flotte.driver.models.DriverLicense;
import com.flotte.driver.models.enums.DriverStatus;
import com.flotte.driver.repositories.DriverLicenseRepository;
import com.flotte.driver.repositories.DriverRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class DriverService {
	private final DriverRepository driverRepository;
	private final DriverLicenseRepository licenseRepository;
	private final DriverEventProducer eventProducer;

	public DriverService(DriverRepository driverRepository, DriverLicenseRepository licenseRepository, DriverEventProducer eventProducer){
		this.driverRepository = driverRepository;
		this.licenseRepository = licenseRepository;
		this.eventProducer = eventProducer;
	}

	public List<DriverResponse> getAllDrivers(DriverStatus status) {
		List<Driver> drivers;

		if(status != null){
			drivers = driverRepository.findAll().stream()
					.filter(driver -> driver.getStatus() == status)
					.toList();
		}else{
			drivers = driverRepository.findAll();
		}
		return drivers.stream()
				.map(this::mapToDriverResponse)
				.collect(Collectors.toList());
	}

	public DriverResponse getDriverById(UUID id){
		Driver driver = findDriverEntityById(id);
		return mapToDriverResponse(driver);
	}

	public DriverResponse createDriver(DriverInput input){
		if(driverRepository.existsByEmail(input.email())){
			throw new IllegalArgumentException("Un conducteur avec cet email existe déjà : " + input.email());
		}
		Driver driver = new Driver();
		driver.setKeycloakUserId((input.keycloakUserId()));
		driver.setFirstName(input.firstName());
		driver.setLastName(input.lastName());
		driver.setEmail(input.email());
		driver.setPhone(input.phone());
		driver.setEmployeeId(input.employeeId());
		// status est ACTIVE par defaut dans l'entité

		Driver savedDriver = driverRepository.save(driver);

		// 1. On fabrique l'événement
		var event = DriverEventFactory.driverCreated(
				savedDriver.getId(),
				savedDriver.getFirstName(),
				savedDriver.getLastName(),
				savedDriver.getEmployeeId(),
				savedDriver.getStatus().name()
		);

		// 2. On publie dans Kafka
		eventProducer.publishDriverEvent(event);

		return mapToDriverResponse(savedDriver);
	}

	public DriverResponse updateDriver(UUID id, DriverUpdate request){
		Driver driver = findDriverEntityById(id);

		if (request.firstName() != null) driver.setFirstName(request.firstName());
		if (request.lastName() != null) driver.setLastName(request.lastName());
		if (request.phone() != null) driver.setPhone(request.phone());
		if (request.employeeId() != null) driver.setEmployeeId(request.employeeId());

		Driver updatedDriver = driverRepository.save(driver);

		var event = DriverEventFactory.driverUpdated(
				updatedDriver.getId(),
				updatedDriver.getFirstName(),
				updatedDriver.getLastName(),
				updatedDriver.getEmployeeId(),
				updatedDriver.getStatus().name()
		);
		eventProducer.publishDriverEvent(event);

		return mapToDriverResponse(updatedDriver);
	}

	public DriverResponse updateDriverStatus(UUID id, DriverStatusInput request){
		Driver driver = findDriverEntityById(id);
		driver.setStatus(request.status());

		Driver updatedDriver = driverRepository.save(driver);

		var event = DriverEventFactory.driverStatusChanged(
				updatedDriver.getId(),
				updatedDriver.getFirstName(),
				updatedDriver.getLastName(),
				updatedDriver.getEmployeeId(),
				driver.getStatus().name(), // ancien status
				request.status().name() // nouveau status
		);
		eventProducer.publishDriverEvent(event);

		return mapToDriverResponse(updatedDriver);
	}

	public void deleteDriver(UUID id){
		Driver driver = findDriverEntityById(id);
		driverRepository.delete(driver);
	}

	public List<LicenseResponse> getDriverLicenses(UUID driverId){
		findDriverEntityById(driverId);
		List<DriverLicense> licenses = licenseRepository.findByDriverId(driverId);
		return licenses.stream()
				.map(this::mapToLicenseResponse)
				.collect(Collectors.toList());
	}

	public LicenseResponse addLicenseToDriver(UUID driverId, LicenseInput request){
		Driver driver = findDriverEntityById(driverId);

		if (licenseRepository.existsByLicenseNumber(request.licenseNumber())) {
			throw new IllegalArgumentException("Ce numéro de permis est déjà enregistré dans le système.");
		}
		DriverLicense license = new DriverLicense();
		license.setLicenseNumber(request.licenseNumber());
		license.setCategory(request.category());
		license.setIssuedDate(request.issuedDate());
		license.setExpiryDate(request.expiryDate());
		license.setCountry(request.country());

		driver.addLicense(license);
		Driver savedDriver = driverRepository.save(driver);
		DriverLicense savedLicense = savedDriver.getLicenses().get(savedDriver.getLicenses().size() - 1);

		return mapToLicenseResponse(savedLicense);
	}

	private DriverResponse mapToDriverResponse(Driver driver) {
		return new DriverResponse(
				driver.getId(),
				driver.getKeycloakUserId(),
				driver.getFirstName(),
				driver.getLastName(),
				driver.getEmail(),
				driver.getPhone(),
				driver.getEmployeeId(),
				driver.getStatus(),
				driver.getCreatedAt(),
				driver.getUpdatedAt()
		);
	}

	private Driver findDriverEntityById(UUID id) {
		return driverRepository.findById(id)
				.or(() -> driverRepository.findByKeycloakUserId(id))
				.orElseThrow(() -> new EntityNotFoundException("Conducteur introuvable avec l'ID : " + id));
	}

	private LicenseResponse mapToLicenseResponse(DriverLicense license) {
		return new LicenseResponse(
				license.getId(),
				license.getDriver().getId(),
				license.getLicenseNumber(),
				license.getCategory(),
				license.getIssuedDate(),
				license.getExpiryDate(),
				license.getCountry(),
				license.getIsValid()
		);
	}
}
