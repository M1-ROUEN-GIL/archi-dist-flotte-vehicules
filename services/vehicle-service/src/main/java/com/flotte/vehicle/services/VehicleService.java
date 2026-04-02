package com.flotte.vehicle.services;

import com.flotte.vehicle.dto.*;
import com.flotte.vehicle.events.VehicleEventFactory;
import com.flotte.vehicle.events.producers.VehicleEventProducer;
import com.flotte.vehicle.models.Vehicle;
import com.flotte.vehicle.models.VehicleAssignment;
import com.flotte.vehicle.models.enums.VehicleStatus;
import com.flotte.vehicle.repositories.VehicleAssignmentRepository;
import com.flotte.vehicle.repositories.VehicleRepository;
import org.springframework.data.domain.Pageable;
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
    // Injection du Repository via le constructeur
    public VehicleService(VehicleRepository repository, VehicleAssignmentRepository assignmentRepository, VehicleEventProducer eventProducer) {
        this.repository = repository;
        this.assignmentRepository = assignmentRepository;
        this.eventProducer = eventProducer;
    }

    // ==========================================
    // 1. LIRE (Tous les véhicules)
    // ==========================================
    public List<VehicleResponse> getAllVehicles() {
        // On récupère uniquement ceux qui ne sont pas supprimés (deletedAt IS NULL)
        return repository.findAllActive()
                .stream()
                .map(this::mapToResponse) // On transforme chaque Entité en DTO
                .collect(Collectors.toList());
    }

    // ==========================================
    // 2. LIRE (Un seul véhicule)
    // ==========================================
    public VehicleResponse getVehicleById(UUID id) {
        Vehicle vehicle = repository.findByIdActive(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Véhicule introuvable"));

        return mapToResponse(vehicle);
    }

    // ==========================================
    // 3. CRÉER
    // ==========================================
    public VehicleResponse createVehicle(VehicleInput input) {
        Vehicle vehicle = new Vehicle();
        vehicle.setPlateNumber(input.plateNumber());
        vehicle.setBrand(input.brand());
        vehicle.setModel(input.model());
        vehicle.setFuelType(input.fuelType());
        vehicle.setMileageKm(input.mileageKm());
        vehicle.setVin(input.vin());
        vehicle.setPayloadCapacityKg(input.payloadCapacityKg());
        vehicle.setCargoVolumeM3(input.cargoVolumeM3());

        // Le status par défaut est géré par l'entité (available)
        Vehicle savedVehicle = repository.save(vehicle);

        // 1. On fabrique le nouvel événement structuré
        var event = VehicleEventFactory.vehicleCreated(
                savedVehicle.getId(),
                savedVehicle.getPlateNumber(),
                savedVehicle.getBrand(),
                savedVehicle.getModel(),
                savedVehicle.getFuelType().name(),
                savedVehicle.getStatus().name(),
                savedVehicle.getMileageKm()
        );
        // 2. On utilise ton Producer abstrait
        eventProducer.publishVehicleEvent(event);

        return mapToResponse(savedVehicle);
    }

    // ==========================================
    // 4. METTRE À JOUR (Infos générales)
    // ==========================================
    public VehicleResponse updateVehicle(UUID id, VehicleUpdate update) {
        Vehicle vehicle = repository.findByIdActive(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Véhicule introuvable"));

        // On met à jour uniquement si la valeur est fournie
        if (update.brand() != null) vehicle.setBrand(update.brand());
        if (update.model() != null) vehicle.setModel(update.model());
        if (update.mileageKm() != null) vehicle.setMileageKm(update.mileageKm());

        Vehicle updatedVehicle = repository.save(vehicle);
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
                vehicle.getStatus().name(), // Ancien statut
                newStatus.name() // Nouveau statut
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

        // Au lieu de faire repository.delete(vehicle), on renseigne la date de suppression
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
        // Vérifier que le véhicule existe
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

        // Vérifier qu'il n'y a pas déjà une assignation active
        assignmentRepository.findActiveByVehicleId(vehicleId).ifPresent(a -> {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Ce véhicule a déjà une assignation active");
        });

        // Vérifier que le véhicule est disponible
        if (vehicle.getStatus() != VehicleStatus.available) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Le véhicule n'est pas disponible");
        }

        // Créer l'assignation
        VehicleAssignment assignment = new VehicleAssignment();
        assignment.setVehicleId(vehicleId);
        assignment.setDriverId(input.driverId());
        assignment.setNotes(input.notes());
        assignment.setCreatedBy(input.createdBy());
        assignment.setStartedAt(OffsetDateTime.now());

        // Passer le véhicule en mission automatiquement
        vehicle.setStatus(VehicleStatus.on_delivery);
        repository.save(vehicle);

        var event = VehicleEventFactory.vehicleAssigned(
                assignment.getId(),
                vehicle.getId(),
                assignment.getDriverId(),
                assignment.getNotes()
        );
        eventProducer.publishAssignmentEvent(event);

        return AssignmentResponse.fromEntity(assignmentRepository.save(assignment));
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

        // Terminer l'assignation
        assignment.setEndedAt(OffsetDateTime.now());
        assignmentRepository.save(assignment);

        var event = VehicleEventFactory.vehicleUnassigned(
                assignment.getId(),
                vehicle.getId(),
                assignment.getDriverId()
        );
        eventProducer.publishAssignmentEvent(event);
        // Remettre le véhicule disponible automatiquement
        vehicle.setStatus(VehicleStatus.available);
            repository.save(vehicle);

        return AssignmentResponse.fromEntity(assignment);
    }

    // ==========================================
    // FONCTION UTILITAIRE (Mapping)
    // ==========================================
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