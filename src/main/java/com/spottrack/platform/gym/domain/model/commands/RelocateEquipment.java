package com.spottrack.platform.gym.domain.model.commands;

import com.spottrack.platform.gym.domain.model.valueobjects.EquipmentId;
import com.spottrack.platform.gym.domain.model.valueobjects.GymId;
import com.spottrack.platform.gym.domain.model.valueobjects.ZoneId;

public record RelocateEquipment(EquipmentId equipmentId, GymId gymId, ZoneId zoneId) {
}
