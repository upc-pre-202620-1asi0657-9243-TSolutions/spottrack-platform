package com.spottrack.platform.monitoring.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

public record MotionSensorResource(
        @Schema Long id,
        @Schema String motionSensorId,
        @Schema String equipmentId,
        @Schema(description = "Name of the equipment this motion sensor is attached to")
        String equipmentName,
        @Schema(description = "Model of the equipment this motion sensor is attached to")
        String equipmentModel,
        @Schema(description = "Status of the equipment this motion sensor is attached to")
        String equipmentStatus,
        @Schema LocalDateTime registeredAt,
        @Schema(description = "Whether the sensor is currently connected to the network")
        boolean online,
        @Schema(description = "Timestamp of the last online/offline status change")
        LocalDateTime lastStatusChangeAt
) {
}
