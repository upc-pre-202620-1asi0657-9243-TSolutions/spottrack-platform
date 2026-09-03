package com.spottrack.platform.maintenance.interfaces.rest.resources;

public record RequestMaintenanceResource(
        String equipmentId,
        String requestedBy,
        String description
) {}
