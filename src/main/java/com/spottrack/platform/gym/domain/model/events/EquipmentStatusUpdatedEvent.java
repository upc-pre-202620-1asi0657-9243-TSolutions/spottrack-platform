package com.spottrack.platform.gym.domain.model.events;

import com.spottrack.platform.gym.domain.model.valueobjects.EquipmentStatus;

/**
 * Domain event published when an equipment status has been updated.
 *
 * @param equipmentId The identity of the equipment whose status changed.
 * @param status      The new status of the equipment.
 */
public record EquipmentStatusUpdatedEvent(String equipmentId, EquipmentStatus status) {
}
