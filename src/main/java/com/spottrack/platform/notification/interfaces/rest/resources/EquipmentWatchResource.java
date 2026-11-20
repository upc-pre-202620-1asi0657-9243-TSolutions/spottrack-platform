package com.spottrack.platform.notification.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Date;

public record EquipmentWatchResource(
        @Schema(description = "Watch identifier", example = "1") Long id,
        @Schema(description = "Watched equipment") String equipmentId,
        @Schema(description = "Watch status", allowableValues = {"ACTIVE", "NOTIFIED", "CLOSED"}) String status,
        @Schema(description = "When the bell was turned on") Date createdAt
) {
}
