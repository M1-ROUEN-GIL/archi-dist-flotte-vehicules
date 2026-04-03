package com.flotte.driver.events.producers;

import com.flotte.driver.events.DriverEventFactory;
import com.flotte.driver.events.DriverPayload;
import com.flotte.driver.events.KafkaEventEnvelope;
import com.flotte.driver.models.DriverLicense;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class DriverEventProducer {

    private static final Logger log = LoggerFactory.getLogger(DriverEventProducer.class);
    private static final String TOPIC = "flotte.conducteurs.events";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public DriverEventProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishDriverEvent(KafkaEventEnvelope<DriverPayload> event) {
        String key = event.payload().driverId().toString();
        kafkaTemplate.send(TOPIC, key, event);
        log.info("Kafka event publié sur {} → type={} driverId={}", TOPIC, event.eventType(), key);
    }

    public void sendLicenseExpiringEvent(DriverLicense license){
        int daysRemaining = (int) java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), license.getExpiryDate());
        var driver = license.getDriver();

        KafkaEventEnvelope<DriverPayload> envelope = DriverEventFactory.licenseExpiring(
                driver.getId(),
                driver.getFirstName(),
                driver.getLastName(),
                driver.getEmployeeId(),
                driver.getStatus().name(),
                license.getLicenseNumber(),
                license.getCategory().name(),
                license.getExpiryDate(),
                daysRemaining
        );
        //envoi a kafka
        kafkaTemplate.send(TOPIC, driver.getId().toString(), envelope);
        log.info("Événement LICENSE_EXPRIRING publié pour le permis {} (Chauffeur: {})",
                    license.getLicenseNumber(), driver.getId());
    }

    public void sendLicenseExpiredEvent(DriverLicense license) {
        var driver = license.getDriver();

        // 2. Utilisation de TA Factory
        KafkaEventEnvelope<DriverPayload> envelope = DriverEventFactory.licenseExpired(
                driver.getId(),
                driver.getFirstName(),
                driver.getLastName(),
                driver.getEmployeeId(),
                driver.getStatus().name(),
                license.getLicenseNumber(),
                license.getCategory().name(),
                license.getExpiryDate()
        );

        // 3. Envoi à Kafka
        kafkaTemplate.send("flotte.conducteurs.events", driver.getId().toString(), envelope);
        log.error("🚨 Événement LICENSE_EXPIRED publié pour le permis {} (Chauffeur: {})",
                license.getLicenseNumber(), driver.getId());
    }

}