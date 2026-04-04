package com.flotte.maintenance;

import org.springframework.boot.test.context.SpringBootTest;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marque un test chargeant le contexte complet avec une base H2, même si le CI
 * définit {@code SPRING_DATASOURCE_URL} (priorité plus faible que ces propriétés).
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@SpringBootTest(properties = {
		"spring.datasource.url=jdbc:h2:mem:maintenance_db;DB_CLOSE_DELAY=-1;MODE=PostgreSQL",
		"spring.datasource.username=sa",
		"spring.datasource.password=",
		"spring.jpa.hibernate.ddl-auto=create-drop"
})
public @interface MaintenanceSpringBootTest {
}
