package com.spottrack.platform.reservation.interfaces.events;

/**
 * Integration event published by the {@code reservation} bounded context when a
 * reservation ends (timer expiry or explicit client action).
 *
 * <p>Other bounded contexts (e.g. {@code monitoring}, to finalize usage tracking
 * for the reserved equipment) must listen to this event rather than to the
 * internal domain event {@code ReservationEndedEvent}.</p>
 *
 * @param reservationId The identity of the reservation that ended.
 * @param equipmentId   The identity of the equipment that was reserved.
 * @param clientId      The identity of the client whose reservation ended.
 */
public record ReservationEndedIntegrationEvent(
        String reservationId,
        String equipmentId,
        Long clientId) {
}
