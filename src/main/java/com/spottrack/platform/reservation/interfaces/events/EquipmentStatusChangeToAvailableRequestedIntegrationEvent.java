package com.spottrack.platform.reservation.interfaces.events;

/**
 * Integration event published by the {@code reservation} bounded context when
 * the equipment must be released back to AVAILABLE.
 *
 * <p>Other bounded contexts (e.g. {@code gym}) must listen to this event rather
 * than to the internal domain event {@code EquipmentStatusChangeToAvailableRequestedEvent}.</p>
 *
 * @param requestId   The identity of the reservation request being released.
 * @param equipmentId The identity of the equipment to mark as AVAILABLE.
 */
public record EquipmentStatusChangeToAvailableRequestedIntegrationEvent(
        String requestId,
        String equipmentId,
        String status) {
}
