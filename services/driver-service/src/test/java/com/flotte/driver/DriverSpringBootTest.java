package com.flotte.driver;

import org.springframework.boot.test.context.SpringBootTest;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Contexte Spring complet avec H2, même si le CI définit {@code SPRING_DATASOURCE_URL}.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@SpringBootTest(properties = {
		"spring.datasource.url=jdbc:h2:mem:drivertest;DB_CLOSE_DELAY=-1;MODE=PostgreSQL",
		"spring.datasource.username=sa",
		"spring.datasource.password=",
		"spring.jpa.hibernate.ddl-auto=create-drop",
		"flotte.postgres.auto-create-databases=false"
})
public @interface DriverSpringBootTest {
}
