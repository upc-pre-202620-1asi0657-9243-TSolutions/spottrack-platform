package com.spottrack.platform.monitoring.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;

public record RegisterMotionSensorResource(
        @Schema(description = "Identifier of the equipment this motion sensor watches")
        String equipmentId
) {
}
