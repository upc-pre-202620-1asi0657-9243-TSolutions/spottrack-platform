package com.spottrack.platform.gym.domain.model.commands;

import com.spottrack.platform.gym.domain.model.valueobjects.EquipmentId;
import com.spottrack.platform.gym.domain.model.valueobjects.ZoneId;

/**
 * This should talk to an alert service, different from the actual relocate command, this one just
 * notifies that it might be convenient to relocate an equipment
 * @param equipmentId
 * @param zoneId
 */
public record RequestEquipmentRelocation(EquipmentId equipmentId, ZoneId zoneId) {
}
