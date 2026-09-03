package com.spottrack.platform.monitoring.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;

public record RegisterCameraSensorResource(
        @Schema(description = "Identifier of the equipment this camera sensor is attached to")
        String equipmentId
) {
}
