package com.flotte.vehicle;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(properties = {
    "spring.security.oauth2.resourceserver.jwt.issuer-uri=http://localhost:8180/realms/gestion-flotte",
    "spring.kafka.enabled=false"
})
@ActiveProfiles("test")
class VehicleServiceApplicationTests {

    @Test
    void contextLoads() {
    }

}
