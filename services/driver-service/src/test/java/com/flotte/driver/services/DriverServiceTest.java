package com.flotte.driver.services;

import com.flotte.driver.repositories.DriverLicenseRepository;
import com.flotte.driver.repositories.DriverRepository;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class DriverServiceTest {
    @Mock
    private DriverRepository driverRepository;

    @Mock
    private DriverLicenseRepository licenseRepository;

    @InjectMocks
    private DriverService driverService;

}
