package com.spottrack.platform.reservation.domain.model.events;

/**
 * Domain event published when an express reservation is created.
 * Re-published as an integration event so other contexts can react to the hold.
 *
 * @param reservationId The identity of the created reservation.
 * @param equipmentId   The identity of the equipment being reserved.
 * @param clientId      The identity of the client making the reservation.
 */
public record ExpressReservationInitiatedEvent(
        String reservationId,
        String equipmentId,
        Long clientId) {
}
