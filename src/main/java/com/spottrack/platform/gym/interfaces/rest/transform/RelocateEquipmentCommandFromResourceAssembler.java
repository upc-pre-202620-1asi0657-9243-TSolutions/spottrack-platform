package com.spottrack.platform.gym.interfaces.rest.transform;

import com.spottrack.platform.gym.domain.model.commands.RelocateEquipment;
import com.spottrack.platform.gym.domain.model.valueobjects.EquipmentId;
import com.spottrack.platform.gym.domain.model.valueobjects.GymId;
import com.spottrack.platform.gym.domain.model.valueobjects.ZoneId;
import com.spottrack.platform.gym.interfaces.rest.resources.RelocateEquipmentResource;

public class RelocateEquipmentCommandFromResourceAssembler {
    public static RelocateEquipment toCommandFromResource(String equipmentId, RelocateEquipmentResource resource) {
        return new RelocateEquipment(
                new EquipmentId(equipmentId),
                new GymId(resource.gymId()),
                new ZoneId(resource.zoneId())
        );
    }
}
