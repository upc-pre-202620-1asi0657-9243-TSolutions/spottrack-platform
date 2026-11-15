package com.spottrack.platform.monitoring.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;

public record RegisterMotionSensorResource(
        @Schema(description = "Identifier of the equipment this motion sensor watches")
        String equipmentId,
        @Schema(description = "Optional payload profile of the sensor's manufacturer (e.g. \"alt\"). "
                + "When omitted, the sensor publishes SpotTrack's default payload format.",
                nullable = true, example = "alt")
        String payloadProfile
) {
}
