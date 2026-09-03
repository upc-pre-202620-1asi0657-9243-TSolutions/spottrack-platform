package com.spottrack.platform.monitoring.domain.model.events;

import com.spottrack.platform.monitoring.domain.model.valueobjects.CameraSensorId;
import com.spottrack.platform.monitoring.domain.model.valueobjects.EquipmentId;

public record CameraSensorRegisteredEvent(CameraSensorId cameraSensorId, EquipmentId equipmentId) {
}
