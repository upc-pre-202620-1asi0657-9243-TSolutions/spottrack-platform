package com.spottrack.platform.maintenance.domain.model.events;

public record MaintenanceJobAcceptedEvent(String maintenanceJobId, String maintenanceId, String technicianId) {}
