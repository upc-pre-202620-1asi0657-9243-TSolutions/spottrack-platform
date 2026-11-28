package com.spottrack.platform.monitoring.interfaces.rest.transform;

import com.spottrack.platform.gym.interfaces.acl.EquipmentSnapshot;
import com.spottrack.platform.monitoring.domain.model.aggregates.CameraSensor;
import com.spottrack.platform.monitoring.interfaces.rest.resources.CameraSensorResource;

public class CameraSensorResourceFromEntity {
    public static CameraSensorResource toResourceFromEntity(CameraSensor entity, EquipmentSnapshot equipment) {
        return new CameraSensorResource(
                entity.getId(),
                entity.getCameraSensorId().uuid(),
                entity.getEquipmentId().uuid(),
                equipment != null ? equipment.equipmentName() : null,
                equipment != null ? equipment.model() : null,
                equipment != null ? equipment.status().name() : null,
                entity.getRegisteredAt()
        );
    }
}
