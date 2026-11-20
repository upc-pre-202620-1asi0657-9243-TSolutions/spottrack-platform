package com.spottrack.platform.notification.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Date;

public record ClientNotificationResource(
        @Schema(description = "Notification identifier", example = "1") Long id,
        @Schema(description = "Equipment the notification is about") String equipmentId,
        @Schema(description = "Kind of notification",
                allowableValues = {"EQUIPMENT_AVAILABLE", "EQUIPMENT_RESTORED", "EQUIPMENT_OCCUPIED_AGAIN"}) String type,
        @Schema(description = "Human-readable message") String message,
        @Schema(description = "Whether the client already read it") boolean read,
        @Schema(description = "When it was read; null while unread") Date readAt,
        @Schema(description = "When it was created") Date createdAt
) {
}
