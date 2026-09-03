package com.spottrack.platform.maintenance.interfaces.rest.resources;

public record MaintenanceJobResource(String id, String maintenanceId, String technicianId, boolean accepted) {
}
