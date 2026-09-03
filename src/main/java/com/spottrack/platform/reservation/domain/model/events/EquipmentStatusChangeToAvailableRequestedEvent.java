package com.spottrack.platform.reservation.domain.model.events;

/**
 * Published when the reservation ends or is cancelled and the equipment must be released.
 * The Equipment bounded context listens to this to mark the equipment back as AVAILABLE.
 */
public record EquipmentStatusChangeToAvailableRequestedEvent(String requestId, String equipmentId) {
}
