package com.spottrack.platform.monitoring.domain.model.events;

import com.spottrack.platform.monitoring.domain.model.valueobjects.EquipmentId;
import com.spottrack.platform.monitoring.domain.model.valueobjects.MotionSensorId;

public record MotionSensorRegisteredEvent(MotionSensorId motionSensorId, EquipmentId equipmentId) {
}
