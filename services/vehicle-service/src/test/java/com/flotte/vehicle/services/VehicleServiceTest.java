package com.flotte.vehicle.services;

import com.flotte.vehicle.dto.*;
import com.flotte.vehicle.events.KafkaEventEnvelope; // 👈 Nouvel import
import com.flotte.vehicle.events.producers.VehicleEventProducer;
import com.flotte.vehicle.models.Vehicle;
import com.flotte.vehicle.models.VehicleAssignment;
import com.flotte.vehicle.models.enums.FuelType;
import com.flotte.vehicle.models.enums.VehicleStatus;
import com.flotte.vehicle.repositories.VehicleAssignmentRepository;
import com.flotte.vehicle.repositories.VehicleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VehicleServiceTest {

    @Mock
    private VehicleRepository repository;

    @Mock
    private VehicleAssignmentRepository assignmentRepository;

    @Mock
    private VehicleEventProducer eventProducer;

    @InjectMocks
    private VehicleService vehicleService;

    private Vehicle vehicle;
    private UUID vehicleId;

    @BeforeEach
    void setUp() {
        vehicleId = UUID.randomUUID();
        vehicle = new Vehicle();
        vehicle.setId(vehicleId);
        vehicle.setPlateNumber("AB-123-CD");
        vehicle.setBrand("Renault");
        vehicle.setModel("Kangoo");
        vehicle.setFuelType(FuelType.electric);
        vehicle.setStatus(VehicleStatus.available);
        vehicle.setMileageKm(10000);
    }

    @Test
    void getAllVehicles_ShouldReturnList() {
        when(repository.findAllActive()).thenReturn(List.of(vehicle));

        List<VehicleResponse> responses = vehicleService.getAllVehicles();

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).id()).isEqualTo(vehicleId);
        verify(repository).findAllActive();
    }

    @Test
    void getVehicleById_WhenFound_ShouldReturnVehicle() {
        when(repository.findByIdActive(vehicleId)).thenReturn(Optional.of(vehicle));

        VehicleResponse response = vehicleService.getVehicleById(vehicleId);

        assertThat(response.id()).isEqualTo(vehicleId);
        verify(repository).findByIdActive(vehicleId);
    }

    @Test
    void getVehicleById_WhenNotFound_ShouldThrowException() {
        when(repository.findByIdActive(vehicleId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> vehicleService.getVehicleById(vehicleId))
                .isInstanceOf(ResponseStatusException.class)
                .hasFieldOrPropertyWithValue("status", HttpStatus.NOT_FOUND);
    }

    @Test
    void createVehicle_ShouldSaveAndPublishEvent() {
        VehicleInput input = new VehicleInput("AB-123-CD", "Renault", "Kangoo", FuelType.electric, 10000, "VIN123", 500, 3.0);
        when(repository.save(any(Vehicle.class))).thenReturn(vehicle);

        VehicleResponse response = vehicleService.createVehicle(input);

        assertThat(response.plateNumber()).isEqualTo("AB-123-CD");
        verify(repository).save(any(Vehicle.class));
        // 👈 Changement ici : publishVehicleEvent au lieu de publish
        verify(eventProducer).publishVehicleEvent(any(KafkaEventEnvelope.class));
    }

    @Test
    void updateVehicle_WhenFound_ShouldUpdateAndSave() {
        VehicleUpdate update = new VehicleUpdate("Peugeot", "Partner", 12000);
        when(repository.findByIdActive(vehicleId)).thenReturn(Optional.of(vehicle));
        when(repository.save(any(Vehicle.class))).thenReturn(vehicle);

        VehicleResponse response = vehicleService.updateVehicle(vehicleId, update);

        assertThat(vehicle.getBrand()).isEqualTo("Peugeot");
        assertThat(vehicle.getModel()).isEqualTo("Partner");
        assertThat(vehicle.getMileageKm()).isEqualTo(12000);
        verify(repository).save(vehicle);
    }

    @Test
    void updateVehicleStatus_WhenFound_ShouldUpdateAndPublishEvent() {
        when(repository.findByIdActive(vehicleId)).thenReturn(Optional.of(vehicle));
        when(repository.save(any(Vehicle.class))).thenReturn(vehicle);

        VehicleResponse response = vehicleService.updateVehicleStatus(vehicleId, VehicleStatus.in_maintenance);

        assertThat(vehicle.getStatus()).isEqualTo(VehicleStatus.in_maintenance);
        verify(repository).save(vehicle);
        // 👈 Changement ici
        verify(eventProducer).publishVehicleEvent(any(KafkaEventEnvelope.class));
    }

    @Test
    void deleteVehicle_WhenFound_ShouldSoftDeleteAndPublishEvent() {
        when(repository.findByIdActive(vehicleId)).thenReturn(Optional.of(vehicle));

        vehicleService.deleteVehicle(vehicleId);

        assertThat(vehicle.getDeletedAt()).isNotNull();
        verify(repository).save(vehicle);
        // 👈 Changement ici
        verify(eventProducer).publishVehicleEvent(any(KafkaEventEnvelope.class));
    }

    @Test
    void getAssignments_WhenVehicleFound_ShouldReturnAssignments() {
        VehicleAssignment assignment = new VehicleAssignment();
        assignment.setVehicleId(vehicleId);
        assignment.setDriverId(UUID.randomUUID());

        when(repository.findByIdActive(vehicleId)).thenReturn(Optional.of(vehicle));
        when(assignmentRepository.findByVehicleIdOrderByStartedAtDesc(vehicleId)).thenReturn(List.of(assignment));

        List<AssignmentResponse> responses = vehicleService.getAssignments(vehicleId);

        assertThat(responses).hasSize(1);
        verify(assignmentRepository).findByVehicleIdOrderByStartedAtDesc(vehicleId);
    }

    @Test
    void createAssignment_WhenAvailable_ShouldCreateAndPublishEvent() {
        UUID driverId = UUID.randomUUID();
        UUID createdBy = UUID.randomUUID();
        AssignmentInput input = new AssignmentInput(driverId, "Notes", createdBy);

        when(repository.findByIdActive(vehicleId)).thenReturn(Optional.of(vehicle));
        when(assignmentRepository.findActiveByVehicleId(vehicleId)).thenReturn(Optional.empty());
        when(assignmentRepository.save(any(VehicleAssignment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AssignmentResponse response = vehicleService.createAssignment(vehicleId, input);

        assertThat(vehicle.getStatus()).isEqualTo(VehicleStatus.on_delivery);
        verify(repository).save(vehicle);
        verify(assignmentRepository).save(any(VehicleAssignment.class));
        // 👈 Changement ici : publishAssignmentEvent pour la partie assignation !
        verify(eventProducer).publishAssignmentEvent(any(KafkaEventEnvelope.class));
    }

    @Test
    void createAssignment_WhenAlreadyAssigned_ShouldThrowConflict() {
        AssignmentInput input = new AssignmentInput(UUID.randomUUID(), "Notes", UUID.randomUUID());

        when(repository.findByIdActive(vehicleId)).thenReturn(Optional.of(vehicle));
        when(assignmentRepository.findActiveByVehicleId(vehicleId)).thenReturn(Optional.of(new VehicleAssignment()));

        assertThatThrownBy(() -> vehicleService.createAssignment(vehicleId, input))
                .isInstanceOf(ResponseStatusException.class)
                .hasFieldOrPropertyWithValue("status", HttpStatus.CONFLICT);
    }

    @Test
    void createAssignment_WhenNotAvailable_ShouldThrowConflict() {
        vehicle.setStatus(VehicleStatus.in_maintenance);
        AssignmentInput input = new AssignmentInput(UUID.randomUUID(), "Notes", UUID.randomUUID());

        when(repository.findByIdActive(vehicleId)).thenReturn(Optional.of(vehicle));
        when(assignmentRepository.findActiveByVehicleId(vehicleId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> vehicleService.createAssignment(vehicleId, input))
                .isInstanceOf(ResponseStatusException.class)
                .hasFieldOrPropertyWithValue("status", HttpStatus.CONFLICT);
    }

    @Test
    void endCurrentAssignment_WhenActiveFound_ShouldEndAndMakeAvailable() {
        VehicleAssignment assignment = new VehicleAssignment();
        assignment.setVehicleId(vehicleId);
        assignment.setDriverId(UUID.randomUUID());

        when(repository.findByIdActive(vehicleId)).thenReturn(Optional.of(vehicle));
        when(assignmentRepository.findActiveByVehicleId(vehicleId)).thenReturn(Optional.of(assignment));

        AssignmentResponse response = vehicleService.endCurrentAssignment(vehicleId);

        assertThat(assignment.getEndedAt()).isNotNull();
        assertThat(vehicle.getStatus()).isEqualTo(VehicleStatus.available);
        verify(assignmentRepository).save(assignment);
        verify(repository).save(vehicle);
        // 👈 Changement ici : publication de la fin d'assignation
        verify(eventProducer).publishAssignmentEvent(any(KafkaEventEnvelope.class));
    }
}