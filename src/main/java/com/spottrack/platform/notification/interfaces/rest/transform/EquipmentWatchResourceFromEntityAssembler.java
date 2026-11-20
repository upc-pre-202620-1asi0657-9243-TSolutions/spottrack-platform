package com.spottrack.platform.notification.interfaces.rest.transform;

import com.spottrack.platform.notification.domain.model.aggregates.EquipmentWatch;
import com.spottrack.platform.notification.interfaces.rest.resources.EquipmentWatchResource;

public final class EquipmentWatchResourceFromEntityAssembler {

    private EquipmentWatchResourceFromEntityAssembler() {}

    public static EquipmentWatchResource toResourceFromEntity(EquipmentWatch watch) {
        return new EquipmentWatchResource(
                watch.getId(),
                watch.getEquipmentId(),
                watch.getStatus().name(),
                watch.getCreatedAt()
        );
    }
}
