package com.flotte.vehicle.config;

import com.flotte.vehicle.models.Vehicle;
import com.flotte.vehicle.models.enums.FuelType;
import com.flotte.vehicle.models.enums.VehicleStatus;
import com.flotte.vehicle.repositories.VehicleRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import net.datafaker.Faker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.UUID;

@Component
@Profile("dev")
public class DataSeeder implements CommandLineRunner {

	/** Même UUID que les requêtes Bruno (historique assignments, etc.) — évite 404 « véhicule introuvable » sur base vide. */
	public static final UUID DEMO_VEHICLE_ID = UUID.fromString("351fbadf-269a-432c-8e79-665fceafe2a5");

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);
    private final VehicleRepository vehicleRepository;

	@PersistenceContext
	private EntityManager entityManager;

    public DataSeeder(VehicleRepository vehicleRepository) {
        this.vehicleRepository = vehicleRepository;
    }

    @Override
	@Transactional
    public void run(String... args) {
		if (!vehicleRepository.existsById(DEMO_VEHICLE_ID)) {
			// INSERT natif : schéma réel = Hibernate ddl-auto (varchar pour enums, pas les types ENUM du script SQL manuel).
			entityManager.createNativeQuery(
					"INSERT INTO vehicles (id, plate_number, brand, model, fuel_type, mileage_km, status, vin, payload_capacity_kg, cargo_volume_m3, metadata, created_at, updated_at) "
							+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW(), NOW())")
					.setParameter(1, DEMO_VEHICLE_ID)
					.setParameter(2, "BR-DEMO-001")
					.setParameter(3, "Renault")
					.setParameter(4, "Kangoo")
					.setParameter(5, FuelType.ELECTRIC.name())
					.setParameter(6, 12000)
					.setParameter(7, VehicleStatus.AVAILABLE.name())
					.setParameter(8, "VF1AAAAAAAAAAAAA1")
					.setParameter(9, 650)
					.setParameter(10, 4.2)
					.setParameter(11, "{}")
					.executeUpdate();
			log.info("Seed: vehicule demo {} (URLs Bruno /api/vehicles/.../assignments).", DEMO_VEHICLE_ID);
		}

		long total = vehicleRepository.count();
		if (total > 1) {
			log.info("Base deja remplie ({} vehicules), seed aleatoire ignore.", total);
			return;
		}

		Faker faker = new Faker(Locale.FRANCE);
		FuelType[] fuels = FuelType.values();
		VehicleStatus[] statuses = {VehicleStatus.AVAILABLE, VehicleStatus.AVAILABLE, VehicleStatus.ON_DELIVERY, VehicleStatus.IN_MAINTENANCE};

		for (int i = 0; i < 9; i++) {
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

		log.info("Seed: 9 vehicules supplementaires (total 10 avec la demo).");
	}
}
