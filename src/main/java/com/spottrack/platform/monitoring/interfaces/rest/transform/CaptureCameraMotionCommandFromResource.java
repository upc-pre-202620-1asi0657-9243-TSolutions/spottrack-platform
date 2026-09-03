package com.spottrack.platform.monitoring.interfaces.rest.transform;

import com.spottrack.platform.monitoring.domain.model.commands.CameraCaptureMotionCommand;
import com.spottrack.platform.monitoring.domain.model.valueobjects.EquipmentId;
import com.spottrack.platform.monitoring.interfaces.rest.resources.CaptureCameraMotionResource;

public class CaptureCameraMotionCommandFromResource {
    public static CameraCaptureMotionCommand toCommandFromResource(CaptureCameraMotionResource resource) {
        return new CameraCaptureMotionCommand(new EquipmentId(resource.equipmentId()), resource.movementDetectedViaVideo());
    }
}
