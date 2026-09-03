package com.spottrack.platform.analytics.interfaces.rest.resources;

public record MaintenanceQuoteResource(
        Long id,
        Long maintenanceQuoteId,
        String equipmentId,
        Double correctiveActionsCost,
        Double sparePartsCost,
        Double preventiveCost,
        Double totalMaintenanceCost
) {}
