package com.spottrack.platform.reservation.interfaces.events;

/**
 * Integration event published by the {@code reservation} bounded context when a
 * reservation is created (express flow).
 *
 * <p>Other bounded contexts (e.g. {@code monitoring}, to start tracking usage of
 * the reserved equipment) must listen to this event rather than to the internal
 * domain event {@code ExpressReservationInitiatedEvent}, which is an internal
 * concern of the {@code reservation} domain.</p>
 *
 * @param reservationId The identity of the created reservation.
 * @param equipmentId   The identity of the equipment being reserved.
 * @param clientId      The identity of the client making the reservation.
 */
public record ExpressReservationInitiatedIntegrationEvent(
        String reservationId,
        String equipmentId,
        Long clientId) {
}
