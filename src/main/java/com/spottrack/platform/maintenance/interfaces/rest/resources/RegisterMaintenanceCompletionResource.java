package com.spottrack.platform.maintenance.interfaces.rest.resources;

import java.math.BigDecimal;

public record RegisterMaintenanceCompletionResource(
        String maintenanceId,
        String notes,
        BigDecimal cost
) {}
