package com.flotte.vehicle.services;

import com.flotte.vehicle.dto.*;
import com.flotte.vehicle.events.VehicleEventFactory;
import com.flotte.vehicle.events.producers.VehicleEventProducer;
import com.flotte.vehicle.models.Vehicle;
import com.flotte.vehicle.models.VehicleAssignment;
import com.flotte.vehicle.models.enums.VehicleStatus;
import com.flotte.vehicle.repositories.VehicleAssignmentRepository;
import com.flotte.vehicle.repositories.VehicleRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class VehicleService {

	private final VehicleRepository repository;
	private final VehicleAssignmentRepository assignmentRepository;
	private final VehicleEventProducer eventProducer;

	public VehicleService(VehicleRepository repository, 
						  VehicleAssignmentRepository assignmentRepository, 
						  VehicleEventProducer eventProducer) {
		this.repository = repository;
		this.assignmentRepository = assignmentRepository;
		this.eventProducer = eventProducer;
	}

	// ==========================================
	// 1. LISTER TOUS LES VÉHICULES
	// ==========================================
	public List<VehicleResponse> getAllVehicles(VehicleStatus status) {
		List<Vehicle> list;
		if (status != null) {
			list = repository.findByStatus(status);
		} else {
			list = repository.findAllActive();
		}

		return list.stream()
				.map(this::mapToResponse)
				.collect(Collectors.toList());
	}

	// ==========================================
	// 2. RÉCUPÉRER UN VÉHICULE PAR SON ID
	// ==========================================
	public VehicleResponse getVehicleById(UUID id) {
		Vehicle vehicle = repository.findByIdActive(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Véhicule introuvable"));
		return mapToResponse(vehicle);
	}

	// ==========================================
	// 3. CRÉER UN VÉHICULE
	// ==========================================
	public VehicleResponse createVehicle(VehicleInput input) {
		if (repository.existsByPlateNumber(input.plateNumber())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cette plaque est déjà enregistrée");
		}

		Vehicle vehicle = new Vehicle();
		vehicle.setPlateNumber(input.plateNumber());
		vehicle.setBrand(input.brand());
		vehicle.setModel(input.model());
		vehicle.setFuelType(input.fuelType());
		vehicle.setMileageKm(input.mileageKm());
		vehicle.setVin(input.vin());
		vehicle.setPayloadCapacityKg(input.payloadCapacityKg());
		vehicle.setCargoVolumeM3(input.cargoVolumeM3());

		Vehicle savedVehicle = repository.save(vehicle);

		var event = VehicleEventFactory.vehicleCreated(
				savedVehicle.getId(),
				savedVehicle.getPlateNumber(),
				savedVehicle.getBrand(),
				savedVehicle.getModel(),
				savedVehicle.getFuelType().name(),
				savedVehicle.getStatus().name(),
				savedVehicle.getMileageKm()
		);
		eventProducer.publishVehicleEvent(event);

		return mapToResponse(savedVehicle);
	}

	// ==========================================
	// 4. METTRE À JOUR UN VÉHICULE
	// ==========================================
	public VehicleResponse updateVehicle(UUID id, VehicleUpdate input) {
		Vehicle vehicle = repository.findByIdActive(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Véhicule introuvable"));

		if (input.brand() != null) vehicle.setBrand(input.brand());
		if (input.model() != null) vehicle.setModel(input.model());
		if (input.mileageKm() != null) vehicle.setMileageKm(input.mileageKm());

		Vehicle updatedVehicle = repository.save(vehicle);

		var event = VehicleEventFactory.vehicleUpdated(
				updatedVehicle.getId(),
				updatedVehicle.getPlateNumber(),
				updatedVehicle.getBrand(),
				updatedVehicle.getModel(),
				updatedVehicle.getFuelType().name(),
				updatedVehicle.getStatus().name(),
				updatedVehicle.getMileageKm()
		);
		eventProducer.publishVehicleEvent(event);

		return mapToResponse(updatedVehicle);
	}

	// ==========================================
	// 5. METTRE À JOUR (Le Statut uniquement)
	// ==========================================
	public VehicleResponse updateVehicleStatus(UUID id, VehicleStatus newStatus) {
		Vehicle vehicle = repository.findByIdActive(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Véhicule introuvable"));

		vehicle.setStatus(newStatus);
		Vehicle updatedVehicle = repository.save(vehicle);

		var event = VehicleEventFactory.vehicleStatusChanged(
				updatedVehicle.getId(),
				updatedVehicle.getPlateNumber(),
				vehicle.getStatus().name(), 
				newStatus.name()
		);
		eventProducer.publishVehicleEvent(event);
		return mapToResponse(updatedVehicle);
	}

	// ==========================================
	// 6. SUPPRIMER (Soft Delete)
	// ==========================================
	public void deleteVehicle(UUID id) {
		Vehicle vehicle = repository.findByIdActive(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Véhicule introuvable"));

		vehicle.setDeletedAt(OffsetDateTime.now());
		repository.save(vehicle);
		var event = VehicleEventFactory.vehicleDeleted(
				vehicle.getId(),
				vehicle.getPlateNumber()
		);
		eventProducer.publishVehicleEvent(event);
	}

	// ==========================================
// 7. LISTER LES ASSIGNATIONS D'UN VÉHICULE
// ==========================================
	public List<AssignmentResponse> getAssignments(UUID vehicleId) {
		repository.findByIdActive(vehicleId)
				.orElseThrow(() -> new ResponseStatusException(
						HttpStatus.NOT_FOUND, "Véhicule introuvable"));

		return assignmentRepository.findByVehicleIdOrderByStartedAtDesc(vehicleId)
				.stream()
				.map(AssignmentResponse::fromEntity)
				.collect(Collectors.toList());
	}

	// ==========================================
// 8. CRÉER UNE ASSIGNATION
// ==========================================
	public AssignmentResponse createAssignment(UUID vehicleId, AssignmentInput input) {
		Vehicle vehicle = repository.findByIdActive(vehicleId)
				.orElseThrow(() -> new ResponseStatusException(
						HttpStatus.NOT_FOUND, "Véhicule introuvable"));

		assignmentRepository.findActiveByVehicleId(vehicleId).ifPresent(a -> {
			throw new ResponseStatusException(
					HttpStatus.CONFLICT, "Ce véhicule a déjà une assignation active");
		});

		if (vehicle.getStatus() != VehicleStatus.AVAILABLE) {
			throw new ResponseStatusException(
					HttpStatus.CONFLICT, "Le véhicule n'est pas disponible");
		}

		VehicleAssignment assignment = new VehicleAssignment();
		assignment.setVehicleId(vehicleId);
		assignment.setDriverId(input.driverId());
		assignment.setNotes(input.notes());
		assignment.setCreatedBy(input.createdBy());
		assignment.setStartedAt(OffsetDateTime.now());

		assignment = assignmentRepository.save(assignment);

		vehicle.setStatus(VehicleStatus.ON_DELIVERY);
		repository.save(vehicle);

		var event = VehicleEventFactory.vehicleAssigned(
				assignment.getId(),
				vehicle.getId(),
				assignment.getDriverId(),
				assignment.getNotes()
		);
		eventProducer.publishAssignmentEvent(event);

		return AssignmentResponse.fromEntity(assignment);
	}

	// ==========================================
// 9. TERMINER L'ASSIGNATION ACTIVE
// ==========================================
	public AssignmentResponse endCurrentAssignment(UUID vehicleId) {
		Vehicle vehicle = repository.findByIdActive(vehicleId)
				.orElseThrow(() -> new ResponseStatusException(
						HttpStatus.NOT_FOUND, "Véhicule introuvable"));

		VehicleAssignment assignment = assignmentRepository.findActiveByVehicleId(vehicleId)
				.orElseThrow(() -> new ResponseStatusException(
						HttpStatus.NOT_FOUND, "Aucune assignation active pour ce véhicule"));

		assignment.setEndedAt(OffsetDateTime.now());
		assignmentRepository.save(assignment);

		var event = VehicleEventFactory.vehicleUnassigned(
				assignment.getId(),
				vehicle.getId(),
				assignment.getDriverId()
		);
		eventProducer.publishAssignmentEvent(event);
		
		vehicle.setStatus(VehicleStatus.AVAILABLE);
		repository.save(vehicle);

		return AssignmentResponse.fromEntity(assignment);
	}

	private VehicleResponse mapToResponse(Vehicle entity) {
		return new VehicleResponse(
				entity.getId(),
				entity.getPlateNumber(),
				entity.getBrand(),
				entity.getModel(),
				entity.getFuelType(),
				entity.getStatus(),
				entity.getMileageKm(),
				entity.getVin(),
				entity.getPayloadCapacityKg(),
				entity.getCargoVolumeM3(),
				entity.getCreatedAt(),
				entity.getUpdatedAt()
		);
	}
}
