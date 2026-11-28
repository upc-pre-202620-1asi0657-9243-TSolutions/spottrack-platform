package com.spottrack.platform.gym.domain.model.events;

import com.spottrack.platform.shared.domain.model.valueobjects.EquipmentStatus;

/**
 * Domain event published when an equipment status has been updated.
 *
 * @param equipmentId    The identity of the equipment whose status changed.
 * @param status         The new status of the equipment.
 * @param previousStatus The status the equipment had before the change; null if unknown.
 */
public record EquipmentStatusUpdatedEvent(String equipmentId, EquipmentStatus status, EquipmentStatus previousStatus) {
}
