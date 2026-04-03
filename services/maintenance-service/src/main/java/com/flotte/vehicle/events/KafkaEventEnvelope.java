package com.flotte.vehicle.events;

import java.time.OffsetDateTime;
import java.util.UUID;

public record KafkaEventEnvelope<T>(
		UUID eventId,
		String eventType,
		String eventVersion,
		OffsetDateTime timestamp,
		T payload,
		EventMetadata metadata
) {}