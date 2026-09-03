package com.spottrack.platform.reservation.domain.model.events;

/**
 * Published when a client cancels a reservation.
 * Downstream: equipment should be released, client may be notified.
 */
public record ReservationCancelledEvent(String reservationId, String equipmentId, Long clientId) {
}
