package com.spottrack.platform.monitoring.interfaces.rest.transform;

import com.spottrack.platform.gym.domain.model.aggregates.Equipment;
import com.spottrack.platform.monitoring.domain.model.aggregates.CameraSensor;
import com.spottrack.platform.monitoring.interfaces.rest.resources.CameraSensorResource;

public class CameraSensorResourceFromEntity {
    public static CameraSensorResource toResourceFromEntity(CameraSensor entity, Equipment equipment) {
        return new CameraSensorResource(
                entity.getId(),
                entity.getCameraSensorId().uuid(),
                entity.getEquipmentId().uuid(),
                equipment != null ? equipment.getEquipmentName() : null,
                equipment != null ? equipment.getModel() : null,
                equipment != null ? equipment.getStatus().name() : null,
                entity.getRegisteredAt()
        );
    }
}
