package com.spottrack.platform.notification.domain.model.queries;

/**
 * Equipments on which the client has the notification bell turned on.
 */
public record GetActiveEquipmentWatchesQuery(Long clientId) {
}
