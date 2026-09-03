package com.spottrack.platform.monitoring.interfaces.rest.transform;

import com.spottrack.platform.gym.domain.model.aggregates.Equipment;
import com.spottrack.platform.monitoring.domain.model.aggregates.MotionSensor;
import com.spottrack.platform.monitoring.interfaces.rest.resources.MotionSensorResource;

public class MotionSensorResourceFromEntity {
    public static MotionSensorResource toResourceFromEntity(MotionSensor entity, Equipment equipment) {
        return new MotionSensorResource(
                entity.getId(),
                entity.getMotionSensorId().uuid(),
                entity.getEquipmentId().uuid(),
                equipment != null ? equipment.getEquipmentName() : null,
                equipment != null ? equipment.getModel() : null,
                equipment != null ? equipment.getStatus().name() : null,
                entity.getRegisteredAt(),
                entity.isOnline(),
                entity.getLastStatusChangeAt()
        );
    }
}
