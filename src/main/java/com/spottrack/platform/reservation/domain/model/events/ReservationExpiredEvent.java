package com.spottrack.platform.reservation.domain.model.events;

/**
 * Published when an express reservation hold elapses before the client checks in.
 */
public record ReservationExpiredEvent(String reservationId, String equipmentId, Long clientId) {
}
