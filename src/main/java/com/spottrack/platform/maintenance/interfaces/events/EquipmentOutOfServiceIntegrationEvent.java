package com.spottrack.platform.maintenance.interfaces.events;

public record EquipmentOutOfServiceIntegrationEvent(String equipmentId, String ticketId, String maintenanceId) {
}
