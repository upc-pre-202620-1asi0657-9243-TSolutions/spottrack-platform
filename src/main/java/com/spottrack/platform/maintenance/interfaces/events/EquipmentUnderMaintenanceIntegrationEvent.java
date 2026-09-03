package com.spottrack.platform.maintenance.interfaces.events;

public record EquipmentUnderMaintenanceIntegrationEvent(String equipmentId, String ticketId) {
}
