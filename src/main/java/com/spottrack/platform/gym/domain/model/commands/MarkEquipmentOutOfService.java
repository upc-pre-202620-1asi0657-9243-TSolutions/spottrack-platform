package com.spottrack.platform.gym.domain.model.commands;

import com.spottrack.platform.gym.domain.model.valueobjects.EquipmentId;

/**
 *
 * @param id
 *
 * Its intended that later, the aggregate modifies the status of the equipment
 */
public record MarkEquipmentOutOfService(
        EquipmentId id
) {
}
