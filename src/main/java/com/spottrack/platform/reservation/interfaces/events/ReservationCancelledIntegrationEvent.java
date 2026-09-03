package com.spottrack.platform.reservation.interfaces.events;

/**
 * Integration event published by the {@code reservation} bounded context when a
 * client cancels a reservation.
 *
 * <p>Other bounded contexts (e.g. {@code monitoring}, to stop usage tracking for
 * the reserved equipment) must listen to this event rather than to the internal
 * domain event {@code ReservationCancelledEvent}.</p>
 *
 * @param reservationId The identity of the cancelled reservation.
 * @param equipmentId   The identity of the equipment that was reserved.
 * @param clientId      The identity of the client who cancelled.
 */
public record ReservationCancelledIntegrationEvent(
        String reservationId,
        String equipmentId,
        Long clientId) {
}
