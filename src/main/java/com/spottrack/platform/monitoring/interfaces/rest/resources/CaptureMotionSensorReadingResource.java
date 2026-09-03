package com.spottrack.platform.monitoring.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;

public record CaptureMotionSensorReadingResource(
        @Schema(description = "Identifier of the equipment this reading applies to")
        String equipmentId,
        @Schema(description = "Whether the sensor detected movement")
        boolean movementDetectedViaSensor
) {
}
