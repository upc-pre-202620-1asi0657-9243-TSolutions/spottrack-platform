package com.spottrack.platform.shared.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Date;

@Schema(
        name = "AlertResponse",
        description = "Alert information response",
        example = "{\"id\": 1, \"equipmentId\": \"...\", \"severity\": \"CRITICAL\", \"message\": \"Equipment out of order\", \"isResolved\": false}"
)
public record AlertResource(

        @Schema(description = "Alert unique identifier", example = "1")
        Long id,

        @Schema(description = "Equipment identifier this alert refers to")
        String equipmentId,

        @Schema(description = "Alert severity", example = "CRITICAL")
        String severity,

        @Schema(description = "Alert message", example = "Equipment out of order")
        String message,

        @Schema(description = "Whether the alert has been resolved", example = "false")
        boolean isResolved,

        @Schema(description = "Alert creation timestamp")
        Date createdAt
) {
}
