package com.spottrack.platform.maintenance.interfaces.rest.resources;

public record MaintenanceResource(
        String id,
        String equipmentId,
        String requestedBy,
        String description
) {}
