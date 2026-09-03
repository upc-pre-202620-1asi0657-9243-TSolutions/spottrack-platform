package com.spottrack.platform.reservation.interfaces.events;

/**
 * Integration event published by the {@code reservation} bounded context when a
 * {@code ReservationRequest} has been successfully submitted and persisted.
 *
 * <p>Other bounded contexts (e.g. {@code gym}) must listen to this event rather
 * than to the internal domain event {@code RequestOccupyEquipmentSubmittedEvent},
 * which is an internal concern of the {@code reservation} domain.</p>
 *
 * @param requestId   The identity of the submitted reservation request.
 * @param equipmentId The identity of the equipment requested to be occupied.
 * @param clientId    The identity of the client who submitted the request.
 */
public record RequestOccupyEquipmentSubmittedIntegrationEvent(
        String requestId,
        String equipmentId) {
}
