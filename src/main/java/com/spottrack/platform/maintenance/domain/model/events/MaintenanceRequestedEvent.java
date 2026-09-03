package com.spottrack.platform.maintenance.domain.model.events;

import com.spottrack.platform.maintenance.domain.model.valueobjects.EquipmentId;

public record MaintenanceRequestedEvent(String maintenanceId, EquipmentId equipmentId, String requestedBy) {}
