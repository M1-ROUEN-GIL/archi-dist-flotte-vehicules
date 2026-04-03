package com.flotte.driver.models;

import com.flotte.driver.models.enums.DriverStatus;
import jakarta.persistence.*;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Table;
import org.hibernate.annotations.*;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "drivers")
// 1. On remplace le DELETE physique par un UPDATE
@SQLDelete(sql = "UPDATE drivers SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
// 2. On filtre par défaut pour ne ramener que les chauffeurs non supprimés
@SQLRestriction("deleted_at IS NULL")
public class Driver {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@Column(name = "keycloak_user_id", unique = true, nullable = false)
	private UUID keycloakUserId;

	@Column(name = "first_name", nullable = false)
	private String firstName;

	@Column(name = "last_name", nullable = false)
	private String lastName;

	@Column(unique = true, nullable = false)
	private String email;

	private String phone;

	@Column(name = "employee_id", unique = true)
	private String employeeId;

	@Enumerated(EnumType.STRING)
	@JdbcTypeCode(SqlTypes.NAMED_ENUM)
	@Column(name = "status", nullable = false)
	private DriverStatus status = DriverStatus.active;

	// Relation 1-à-N : Un chauffeur peut avoir plusieurs permis
	@OneToMany(mappedBy = "driver", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<DriverLicense> licenses = new ArrayList<>();

	@CreationTimestamp
	@Column(name = "created_at", nullable = false, updatable = false)
	private OffsetDateTime createdAt = OffsetDateTime.now();

	@UpdateTimestamp
	@Column(name = "updated_at", nullable = false)
	private OffsetDateTime updatedAt = OffsetDateTime.now();

	@Column(name = "deleted_at")
	private OffsetDateTime deletedAt;

	public Driver(){
	}
	// ==========================================
	// GETTERS (Accessibles pour tous les champs)
	// ==========================================

	public UUID getId() { return id; }
	public UUID getKeycloakUserId() { return keycloakUserId; }
	public String getFirstName() { return firstName; }
	public String getLastName() { return lastName; }
	public String getEmail() { return email; }
	public String getPhone() { return phone; }
	public String getEmployeeId() { return employeeId; }
	public DriverStatus getStatus() { return status; }
	public List<DriverLicense> getLicenses() { return licenses; }
	public OffsetDateTime getCreatedAt() { return createdAt; }
	public OffsetDateTime getUpdatedAt() { return updatedAt; }
	public OffsetDateTime getDeletedAt() { return deletedAt; }


	// ==========================================
	// SETTERS (Uniquement pour les champs métier)
	// ==========================================

	public void setKeycloakUserId(UUID keycloakUserId) { this.keycloakUserId = keycloakUserId; }
	public void setFirstName(String firstName) { this.firstName = firstName; }
	public void setLastName(String lastName) { this.lastName = lastName; }
	public void setEmail(String email) { this.email = email; }
	public void setPhone(String phone) { this.phone = phone; }
	public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }
	public void setStatus(DriverStatus status) { this.status = status; }

	// Le setter de deletedAt est utile si tu gères le soft delete à la main dans le Service
	public void setDeletedAt(OffsetDateTime deletedAt) { this.deletedAt = deletedAt; }


	// ==========================================
	// MÉTHODES UTILITAIRES (Relation bidirectionnelle)
	// ==========================================

	public void addLicense(DriverLicense license) {
		licenses.add(license);
		license.setDriver(this);
	}

	public void removeLicense(DriverLicense license) {
		licenses.remove(license);
		license.setDriver(null);
	}
}