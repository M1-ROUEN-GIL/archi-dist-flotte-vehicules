package com.flotte.maintenance.config;

import com.flotte.maintenance.model.MaintenancePriority;
import com.flotte.maintenance.model.MaintenanceRecord;
import com.flotte.maintenance.model.MaintenanceStatus;
import com.flotte.maintenance.model.MaintenanceType;
import com.flotte.maintenance.repository.MaintenanceRepository;
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
    private final MaintenanceRepository maintenanceRepository;

    public DataSeeder(MaintenanceRepository maintenanceRepository) {
        this.maintenanceRepository = maintenanceRepository;
    }

    @Override
    public void run(String... args) {
        if (maintenanceRepository.count() > 0) {
            log.info("Maintenances deja presentes, seed ignore.");
            return;
        }

        Faker faker = new Faker(Locale.FRANCE);
        MaintenanceType[] types = MaintenanceType.values();
        MaintenancePriority[] priorities = MaintenancePriority.values();
        String[] descriptions = {
            "Revision periodique",
            "Changement plaquettes de frein",
            "Remplacement pneus",
            "Vidange moteur et filtres",
            "Controle technique",
            "Reparation climatisation",
            "Changement batterie",
            "Revision courroie de distribution"
        };

        for (int i = 0; i < 12; i++) {
            MaintenanceRecord r = new MaintenanceRecord();
            r.setVehicleId(UUID.randomUUID());
            r.setType(faker.options().option(types));
            r.setPriority(faker.options().option(priorities));
            r.setStatus(faker.options().option(MaintenanceStatus.SCHEDULED, MaintenanceStatus.SCHEDULED, MaintenanceStatus.IN_PROGRESS, MaintenanceStatus.COMPLETED));
            r.setScheduledDate(LocalDate.now().plusDays(faker.number().numberBetween(-30, 60)));
            r.setDescription(faker.options().option(descriptions));
            r.setMileageAtService(faker.number().numberBetween(5000, 100000));

            if (r.getStatus() == MaintenanceStatus.COMPLETED) {
                r.setCompletedDate(r.getScheduledDate().plusDays(faker.number().numberBetween(0, 3)));
                r.setCostEur(java.math.BigDecimal.valueOf(faker.number().randomDouble(2, 50, 1500)));
                r.setNextServiceKm(r.getMileageAtService() + faker.number().numberBetween(10000, 30000));
            }

            maintenanceRepository.save(r);
        }

        log.info("Seed: 12 maintenances generees.");
    }
}
