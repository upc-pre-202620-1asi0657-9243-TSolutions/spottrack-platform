package com.spottrack.platform.reservation.domain.model.events;

/**
 * Domain event published when an express reservation is created.
 * Triggers the SubmitRequestOccupyEquipment command to mark the equipment as OCCUPIED.
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
