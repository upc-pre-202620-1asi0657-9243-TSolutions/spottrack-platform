package com.spottrack.platform.gym.interfaces.rest.transform;

import com.spottrack.platform.gym.domain.model.aggregates.Equipment;
import com.spottrack.platform.gym.interfaces.rest.resources.EquipmentResource;

public class EquipmentResourceFromEntityAssembler {
    public static EquipmentResource toResourceFromEntity(Equipment equipment){
        return new EquipmentResource(
                equipment.getId().uuid(),
                equipment.getEquipmentName(),
                equipment.getStatus(),
                equipment.getModel(),
                equipment.getManufacturerId().uuid(),
                equipment.getZoneId().uuid(),
                equipment.getPurchasePrice().currency(),
                equipment.getPurchasePrice().amount(),
                equipment.getMaintenanceThreshold()
        );
    }
}
