package com.spottrack.platform.gym.interfaces.rest.transform;

import com.spottrack.platform.gym.domain.model.commands.DefineMaintenanceThresholdCommand;
import com.spottrack.platform.gym.domain.model.valueobjects.EquipmentId;
import com.spottrack.platform.gym.interfaces.rest.resources.DefineMaintenanceThresholdResource;

public class DefineMaintenanceThresholdCommandFromResourceAssembler {
    public static DefineMaintenanceThresholdCommand toCommandFromResource(String equipmentId, DefineMaintenanceThresholdResource resource){
        return new DefineMaintenanceThresholdCommand(new EquipmentId(equipmentId), resource.threshold());
    }
}
