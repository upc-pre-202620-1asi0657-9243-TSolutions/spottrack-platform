package com.spottrack.platform.gym.interfaces.rest.resources;

public record ZoneResource(String zoneId, String zoneName, int maximumOccupancy, String branchId) {
}
