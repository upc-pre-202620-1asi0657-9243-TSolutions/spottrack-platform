package com.spottrack.platform.monitoring.interfaces.rest.transform;

import com.spottrack.platform.monitoring.domain.model.commands.RegisterCameraSensorCommand;
import com.spottrack.platform.monitoring.interfaces.rest.resources.RegisterCameraSensorResource;

public class RegisterCameraSensorCommandFromResource {
    public static RegisterCameraSensorCommand toCommandFromResource(RegisterCameraSensorResource resource) {
        return new RegisterCameraSensorCommand(resource.equipmentId());
    }
}
