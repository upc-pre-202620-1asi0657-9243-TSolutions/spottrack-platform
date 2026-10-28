package com.spottrack.platform.reservation.interfaces.events;

/**
 * Integration event published by the {@code reservation} bounded context when an
 * express reservation hold expires without a check-in.
 *
 * <p>Other bounded contexts (e.g. {@code gym}, to release the held equipment, and
 * {@code monitoring}, to close the session tracker) must listen to this event rather
 * than to the internal domain event {@code ReservationExpiredEvent}.</p>
 *
 * @param reservationId The identity of the reservation that expired.
 * @param equipmentId   The identity of the equipment that was held.
 * @param clientId      The identity of the client whose hold expired.
 */
public record ReservationExpiredIntegrationEvent(
        String reservationId,
        String equipmentId,
        Long clientId) {
}
