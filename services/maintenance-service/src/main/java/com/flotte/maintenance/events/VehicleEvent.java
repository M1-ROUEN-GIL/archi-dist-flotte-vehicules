package com.flotte.maintenance.events;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record VehicleEvent(
    String eventType,
    UUID vehicleId,
    Integer mileageKm
) {}
