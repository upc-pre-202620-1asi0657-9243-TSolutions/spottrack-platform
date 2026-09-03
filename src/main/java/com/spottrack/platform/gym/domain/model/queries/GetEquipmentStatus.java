package com.spottrack.platform.gym.domain.model.queries;

import com.spottrack.platform.gym.domain.model.valueobjects.EquipmentId;
import com.spottrack.platform.gym.domain.model.valueobjects.EquipmentStatus;

public record GetEquipmentStatus(
        EquipmentId equipmentId
) {
}
