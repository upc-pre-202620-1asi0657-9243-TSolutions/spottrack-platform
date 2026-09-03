package com.spottrack.platform.reservation.domain.model.events;

/**
 * Published when a client explicitly ends a reservation early.
 * The diagram shows this triggers a notification to the Alerts bounded context.
 * Equipment should also be released back to AVAILABLE.
 */
public record ReservationEndedEvent(String reservationId, String equipmentId, Long clientId) {
}
