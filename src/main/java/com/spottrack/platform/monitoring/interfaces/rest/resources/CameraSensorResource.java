package com.spottrack.platform.monitoring.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

public record CameraSensorResource(
        @Schema Long id,
        @Schema String cameraSensorId,
        @Schema String equipmentId,
        @Schema(description = "Name of the equipment this camera sensor is attached to")
        String equipmentName,
        @Schema(description = "Model of the equipment this camera sensor is attached to")
        String equipmentModel,
        @Schema(description = "Status of the equipment this camera sensor is attached to")
        String equipmentStatus,
        @Schema LocalDateTime registeredAt
) {
}
