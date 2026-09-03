package com.spottrack.platform.gym.interfaces.rest.transform;

import com.spottrack.platform.gym.domain.model.commands.UpdateEquipmentStatus;
import com.spottrack.platform.gym.interfaces.rest.resources.UpdateEquipmentStatusResource;

public class UpdateEquipmentStatusCommandFromResourceAssembler {
    public static UpdateEquipmentStatus toCommandFromResource(UpdateEquipmentStatusResource resource){
        return new UpdateEquipmentStatus(resource.id(), resource.status());
    }
}
