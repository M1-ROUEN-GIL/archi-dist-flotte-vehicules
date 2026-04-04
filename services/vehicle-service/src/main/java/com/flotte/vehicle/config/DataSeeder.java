package com.flotte.vehicle.config;

import com.flotte.vehicle.models.Vehicle;
import com.flotte.vehicle.models.enums.FuelType;
import com.flotte.vehicle.models.enums.VehicleStatus;
import com.flotte.vehicle.repositories.VehicleRepository;
import net.datafaker.Faker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
@Profile("dev")
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);
    private final VehicleRepository vehicleRepository;

    public DataSeeder(VehicleRepository vehicleRepository) {
        this.vehicleRepository = vehicleRepository;
    }

    @Override
    public void run(String... args) {
        if (vehicleRepository.count() > 0) {
            log.info("Vehicles deja presents, seed ignore.");
            return;
        }

        Faker faker = new Faker(Locale.FRANCE);
        FuelType[] fuels = FuelType.values();
        VehicleStatus[] statuses = {VehicleStatus.AVAILABLE, VehicleStatus.AVAILABLE, VehicleStatus.ON_DELIVERY, VehicleStatus.IN_MAINTENANCE};

        for (int i = 0; i < 10; i++) {
            Vehicle v = new Vehicle();
            v.setPlateNumber(faker.regexify("[A-Z]{2}-[0-9]{3}-[A-Z]{2}"));
            v.setBrand(faker.options().option("Renault", "Peugeot", "Citroen", "Mercedes", "Iveco", "Fiat"));
            v.setModel(faker.options().option("Kangoo", "Expert", "Berlingo", "Sprinter", "Daily", "Ducato"));
            v.setFuelType(faker.options().option(fuels));
            v.setMileageKm(faker.number().numberBetween(1000, 120000));
            v.setStatus(faker.options().option(statuses));
            v.setVin(faker.regexify("[A-HJ-NPR-Z0-9]{17}"));
            v.setPayloadCapacityKg(faker.number().numberBetween(400, 2000));
            v.setCargoVolumeM3(faker.number().randomDouble(1, 2, 15));
            vehicleRepository.save(v);
        }

        log.info("Seed: 10 vehicules generes.");
    }
}
