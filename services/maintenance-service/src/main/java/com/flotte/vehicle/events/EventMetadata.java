package com.flotte.vehicle.events;

import java.util.UUID;

public record EventMetadata(
		UUID correlationId,
		String triggeredBy
) {}