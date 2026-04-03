package com.flotte.driver.models;

import com.flotte.driver.models.enums.LicenseCategory;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Formula;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "driver_licenses")
public class DriverLicense {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@Column(name = "license_number", unique = true, nullable = false)
	private String licenseNumber;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private LicenseCategory category;

	@Column(name = "issued_date", nullable = false)
	private LocalDate issuedDate;

	@Column(name = "expiry_date", nullable = false)
	private LocalDate expiryDate;

	@Column(nullable = false, length = 2)
	private String country = "FR";

	// Hibernate va exécuter ce SQL en temps réel quand tu récupères le permis
	@Formula("expiry_date > CURRENT_DATE")
	private Boolean isValid;

	// Relation N-à-1 : Plusieurs permis peuvent appartenir à un même chauffeur
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "driver_id", nullable = false)
	private Driver driver;

	@CreationTimestamp
	@Column(name = "created_at", nullable = false, updatable = false)
	private OffsetDateTime createdAt = OffsetDateTime.now();

	public DriverLicense(){
	}
	// ==========================================
	// GETTERS (Accessibles pour tous les champs)
	// ==========================================

	public UUID getId() { return id; }
	public String getLicenseNumber() { return licenseNumber; }
	public LicenseCategory getCategory() { return category; }
	public LocalDate getIssuedDate() { return issuedDate; }
	public LocalDate getExpiryDate() { return expiryDate; }
	public String getCountry() { return country; }
	public Boolean getIsValid() { return isValid; }
	public Driver getDriver() { return driver; }
	public OffsetDateTime getCreatedAt() { return createdAt; }


	// ==========================================
	// SETTERS (Uniquement pour les champs métier)
	// ==========================================

	public void setLicenseNumber(String licenseNumber) { this.licenseNumber = licenseNumber; }
	public void setCategory(LicenseCategory category) { this.category = category; }
	public void setIssuedDate(LocalDate issuedDate) { this.issuedDate = issuedDate; }
	public void setExpiryDate(LocalDate expiryDate) { this.expiryDate = expiryDate; }
	public void setCountry(String country) { this.country = country; }

	// Ce setter est crucial pour que la méthode addLicense() de Driver fonctionne
	public void setDriver(Driver driver) { this.driver = driver; }
}