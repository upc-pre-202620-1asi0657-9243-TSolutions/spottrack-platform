package com.spottrack.platform.monitoring.interfaces.rest.transform;

import com.spottrack.platform.monitoring.domain.model.commands.RegisterMotionSensorCommand;
import com.spottrack.platform.monitoring.interfaces.rest.resources.RegisterMotionSensorResource;

public class RegisterMotionSensorCommandFromResource {
    public static RegisterMotionSensorCommand toCommandFromResource(RegisterMotionSensorResource resource) {
        return new RegisterMotionSensorCommand(resource.equipmentId());
    }
}
