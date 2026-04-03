package com.flotte.driver.services;

import com.flotte.driver.dto.*;
import com.flotte.driver.events.producers.DriverEventProducer;
import com.flotte.driver.models.Driver;
import com.flotte.driver.models.enums.DriverStatus;
import com.flotte.driver.repositories.DriverLicenseRepository;
import com.flotte.driver.repositories.DriverRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DriverServiceTest {
	@Mock
	private DriverRepository driverRepository;

	@Mock
	private DriverLicenseRepository licenseRepository;

	@Mock
	private DriverEventProducer eventProducer;

	@InjectMocks
	private DriverService driverService;

	private Driver driver;
	private UUID driverId;

	@BeforeEach
	void setUp() {
		driverId = UUID.randomUUID();
		driver = new Driver();
		driver.setId(driverId);
		driver.setFirstName("John");
		driver.setLastName("Doe");
		driver.setEmail("john.doe@example.com");
		driver.setStatus(DriverStatus.active);
	}

	@Test
	void getAllDrivers_ShouldReturnList() {
		when(driverRepository.findAll()).thenReturn(List.of(driver));
		
		List<DriverResponse> result = driverService.getAllDrivers(null);
		
		assertNotNull(result);
		assertEquals(1, result.size());
		assertEquals("John", result.get(0).firstName());
	}

	@Test
	void getDriverById_WhenFound_ShouldReturnDriver() {
		when(driverRepository.findById(driverId)).thenReturn(Optional.of(driver));
		
		DriverResponse result = driverService.getDriverById(driverId);
		
		assertNotNull(result);
		assertEquals("John", result.firstName());
	}

	@Test
	void createDriver_ShouldSaveAndPublishEvent() {
		DriverInput input = new DriverInput(UUID.randomUUID(), "Jane", "Smith", "jane@example.com", "123456", "EMP001");
		when(driverRepository.existsByEmail(any())).thenReturn(false);
		when(driverRepository.save(any())).thenAnswer(invocation -> {
			Driver d = invocation.getArgument(0);
			d.setId(UUID.randomUUID());
			return d;
		});

		DriverResponse result = driverService.createDriver(input);

		assertNotNull(result);
		verify(driverRepository).save(any());
		verify(eventProducer).publishDriverEvent(any());
	}

	@Test
	void updateDriver_WhenFound_ShouldUpdateAndPublishEvent() {
		DriverUpdate update = new DriverUpdate("Jane", "Doe", "987654", "EMP002");
		when(driverRepository.findById(driverId)).thenReturn(Optional.of(driver));
		when(driverRepository.save(any())).thenReturn(driver);

		driverService.updateDriver(driverId, update);

		assertEquals("Jane", driver.getFirstName());
		assertEquals("Doe", driver.getLastName());
		verify(eventProducer).publishDriverEvent(any());
	}

	@Test
	void deleteDriver_WhenFound_ShouldDelete() {
		when(driverRepository.findById(driverId)).thenReturn(Optional.of(driver));

		driverService.deleteDriver(driverId);

		verify(driverRepository).delete(driver);
	}

	@Test
	void getDriverLicenses_ShouldReturnList() {
		when(driverRepository.findById(driverId)).thenReturn(Optional.of(driver));
		when(licenseRepository.findByDriverId(driverId)).thenReturn(List.of());

		List<LicenseResponse> result = driverService.getDriverLicenses(driverId);

		assertNotNull(result);
		verify(licenseRepository).findByDriverId(driverId);
	}

	@Test
	void addLicenseToDriver_ShouldSave() {
		LicenseInput input = new LicenseInput("LIC123", "B", java.time.LocalDate.now(), java.time.LocalDate.now().plusYears(10), "FR");
		when(driverRepository.findById(driverId)).thenReturn(Optional.of(driver));
		when(licenseRepository.existsByLicenseNumber(any())).thenReturn(false);
		when(driverRepository.save(any())).thenReturn(driver);

		// Manually add a license to the list to simulate JPA saving and returning the updated entity
		com.flotte.driver.models.DriverLicense license = new com.flotte.driver.models.DriverLicense();
		license.setDriver(driver);
		driver.addLicense(license);

		LicenseResponse result = driverService.addLicenseToDriver(driverId, input);

		assertNotNull(result);
		verify(driverRepository).save(any());
	}
}
