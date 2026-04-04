package com.flotte.driver.config;

import com.flotte.driver.models.Driver;
import com.flotte.driver.models.DriverLicense;
import com.flotte.driver.models.enums.DriverStatus;
import com.flotte.driver.models.enums.LicenseCategory;
import com.flotte.driver.repositories.DriverRepository;
import net.datafaker.Faker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Locale;
import java.util.UUID;

@Component
@Profile("dev")
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);
    private final DriverRepository driverRepository;

    public DataSeeder(DriverRepository driverRepository) {
        this.driverRepository = driverRepository;
    }

    @Override
    public void run(String... args) {
        if (driverRepository.count() > 0) {
            log.info("Drivers deja presents, seed ignore.");
            return;
        }

        Faker faker = new Faker(Locale.FRANCE);
        LicenseCategory[] categories = {LicenseCategory.B, LicenseCategory.B, LicenseCategory.C, LicenseCategory.CE};

        for (int i = 0; i < 8; i++) {
            Driver d = new Driver();
            d.setKeycloakUserId(UUID.randomUUID());
            d.setFirstName(faker.name().firstName());
            d.setLastName(faker.name().lastName());
            d.setEmail(faker.internet().emailAddress());
            d.setPhone(faker.regexify("\\+336[0-9]{8}"));
            d.setEmployeeId("EMP-" + faker.regexify("[0-9]{4}"));
            d.setStatus(faker.options().option(DriverStatus.ACTIVE, DriverStatus.ACTIVE, DriverStatus.ON_TOUR, DriverStatus.ON_LEAVE));

            DriverLicense license = new DriverLicense();
            license.setLicenseNumber(faker.regexify("[0-9]{2}[A-Z]{2}[0-9]{5}"));
            license.setCategory(faker.options().option(categories));
            license.setIssuedDate(LocalDate.now().minusYears(faker.number().numberBetween(1, 10)));
            license.setExpiryDate(LocalDate.now().plusYears(faker.number().numberBetween(1, 10)));
            license.setCountry("FR");
            d.addLicense(license);

            driverRepository.save(d);
        }

        log.info("Seed: 8 conducteurs generes avec permis.");
    }
}
