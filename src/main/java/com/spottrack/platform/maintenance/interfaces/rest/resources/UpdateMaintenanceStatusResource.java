package com.spottrack.platform.maintenance.interfaces.rest.resources;

import com.spottrack.platform.maintenance.domain.model.valueobjects.MaintenanceStatus;

public record UpdateMaintenanceStatusResource(MaintenanceStatus newStatus) {}
