package com.flotte.driver.jobs;

import com.flotte.driver.events.producers.DriverEventProducer;
import com.flotte.driver.repositories.DriverLicenseRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Component
public class LicenseScheduler {

    private static final Logger log = LoggerFactory.getLogger(LicenseScheduler.class);
    private final DriverLicenseRepository licenseRepository;
    private final DriverEventProducer eventProducer;

    public LicenseScheduler(DriverLicenseRepository licenseRepository, DriverEventProducer eventProducer) {
        this.licenseRepository = licenseRepository;
        this.eventProducer = eventProducer;
    }

    @Scheduled(cron = "0 0 0 * * *") // "0 0 0 * * *" = Tous les jours à 00h00
    //@Scheduled(fixedRate = 10000)// S'exécute toutes les 10 secondes pour tester
    @Transactional(readOnly = true) // dire à Spring Boot de garder la connexion (la session) ouverte pendant toute l'exécution de la méthode
    //sinon probleme la config FetchType.LAZY
    public void checkLicenseExpirations() {
        log.info("⏰ Démarrage de la vérification quotidienne des permis...");

        // 1. Calcul de la date dans 30 jours exacts
        LocalDate in30Days = LocalDate.now().plusDays(30);

        licenseRepository.findByExpiryDate(in30Days).forEach(license -> {
            log.warn("⚠️ Le permis {} expire dans 30 jours !", license.getLicenseNumber());
            eventProducer.sendLicenseExpiringEvent(license);
        });

        // 2. Calcul de la date d'aujourd'hui
        LocalDate today = LocalDate.now();

        licenseRepository.findByExpiryDate(today).forEach(license -> {
            log.error("🚫 Le permis {} est désormais EXPIRÉ !", license.getLicenseNumber());
            eventProducer.sendLicenseExpiredEvent(license);
        });
    }
}