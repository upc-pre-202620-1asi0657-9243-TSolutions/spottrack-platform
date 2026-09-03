package com.spottrack.platform.monitoring.interfaces.rest.transform;

import com.spottrack.platform.monitoring.domain.model.commands.MotionSensorCaptureCommand;
import com.spottrack.platform.monitoring.domain.model.valueobjects.EquipmentId;
import com.spottrack.platform.monitoring.interfaces.rest.resources.CaptureMotionSensorReadingResource;

public class CaptureMotionSensorReadingCommandFromResource {
    public static MotionSensorCaptureCommand toCommandFromResource(CaptureMotionSensorReadingResource resource) {
        return new MotionSensorCaptureCommand(new EquipmentId(resource.equipmentId()), resource.movementDetectedViaSensor());
    }
}
