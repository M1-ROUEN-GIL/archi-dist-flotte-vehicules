package com.flotte.vehicle.events;

public record StatusChange(
		String previous,
		String current
) {}