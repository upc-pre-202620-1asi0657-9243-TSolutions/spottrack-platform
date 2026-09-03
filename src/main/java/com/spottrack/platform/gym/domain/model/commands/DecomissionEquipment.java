package com.spottrack.platform.gym.domain.model.commands;

import com.spottrack.platform.gym.domain.model.valueobjects.EquipmentId;
import com.spottrack.platform.gym.domain.model.valueobjects.EquipmentStatus;

/**
 * Changes the state of an equipment to decommissioned
 */

public record DecomissionEquipment(
        EquipmentId equipmentId, EquipmentStatus equipmentStatus
) {

}
