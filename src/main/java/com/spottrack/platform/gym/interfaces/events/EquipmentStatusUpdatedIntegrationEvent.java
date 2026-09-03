package com.spottrack.platform.gym.interfaces.events;

import com.spottrack.platform.gym.domain.model.valueobjects.EquipmentStatus;

/**
 * Integration event published by the {@code gym} bounded context when an equipment
 * status has been updated.
 *
 * <p>Other bounded contexts (e.g. {@code analytics}, {@code monitoring}) must listen
 * to this event rather than to internal domain events.</p>
 *
 * @param equipmentId The identity of the equipment whose status changed.
 * @param status      The new status of the equipment.
 */
public record EquipmentStatusUpdatedIntegrationEvent(String equipmentId, EquipmentStatus status) {
}
