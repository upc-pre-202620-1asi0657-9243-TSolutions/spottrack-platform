package com.spottrack.platform.monitoring.interfaces.rest.transform;

import com.spottrack.platform.gym.interfaces.acl.EquipmentSnapshot;
import com.spottrack.platform.monitoring.domain.model.aggregates.MotionSensor;
import com.spottrack.platform.monitoring.interfaces.rest.resources.MotionSensorResource;

public class MotionSensorResourceFromEntity {
    public static MotionSensorResource toResourceFromEntity(MotionSensor entity, EquipmentSnapshot equipment) {
        return new MotionSensorResource(
                entity.getId(),
                entity.getMotionSensorId().uuid(),
                entity.getEquipmentId().uuid(),
                equipment != null ? equipment.equipmentName() : null,
                equipment != null ? equipment.model() : null,
                equipment != null ? equipment.status().name() : null,
                entity.getRegisteredAt(),
                entity.isOnline(),
                entity.getLastStatusChangeAt(),
                entity.getLastSeenAt(),
                entity.getPayloadProfile()
        );
    }
}
