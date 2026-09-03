package com.spottrack.platform.reservation.domain.model.events;

/**
 * Published when a ReservationRequest is created.
 * The Equipment bounded context listens to this to mark the equipment as RESERVED.
 */
public record RequestOccupyEquipmentSubmittedEvent(String requestId, String equipmentId, Long clientId) {
}
