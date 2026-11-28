package com.spottrack.platform.gym.interfaces.acl;

import com.spottrack.platform.shared.domain.model.valueobjects.EquipmentStatus;

/**
 * Read-only view of a piece of equipment for other bounded contexts, so they do not
 * depend on the gym's Equipment aggregate.
 *
 * @param equipmentId   the equipment UUID
 * @param equipmentName display name
 * @param model         model reference, may be null
 * @param status        current status
 * @param zoneId        UUID of the zone it is placed in, null if not placed
 */
public record EquipmentSnapshot(
        String equipmentId,
        String equipmentName,
        String model,
        EquipmentStatus status,
        String zoneId) {
}
