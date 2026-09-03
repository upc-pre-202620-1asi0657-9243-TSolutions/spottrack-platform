package com.spottrack.platform.maintenance.interfaces.rest.resources;

public record CreateTechnicalTicketResource(
        String maintenanceId,
        String priority,
        String type
) {}
