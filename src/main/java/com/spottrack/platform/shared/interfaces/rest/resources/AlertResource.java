package com.spottrack.platform.shared.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Date;

@Schema(
        name = "AlertResponse",
        description = "Alert information response",
        example = "{\"id\": 1, \"equipmentId\": \"...\", \"severity\": \"CRITICAL\", \"message\": \"Anomaly reported: ...\", \"isResolved\": false, \"createdAt\": \"2026-10-27T10:15:00.000+00:00\", \"source\": \"ANOMALY\", \"resolvedAt\": null}"
)
public record AlertResource(

        @Schema(description = "Alert unique identifier", example = "1")
        Long id,

        @Schema(description = "Equipment identifier this alert refers to")
        String equipmentId,

        @Schema(description = "Alert severity", example = "CRITICAL", allowableValues = {"WARNING", "CRITICAL"})
        String severity,

        @Schema(description = "Alert message", example = "Equipment out of order")
        String message,

        @Schema(description = "Whether the alert has been resolved", example = "false")
        boolean isResolved,

        @Schema(description = "Alert creation timestamp")
        Date createdAt,

        @Schema(description = "Event that generated the alert. Null for alerts created before the source was tracked.",
                example = "ANOMALY", allowableValues = {"MAINTENANCE", "SENSOR", "ANOMALY"}, nullable = true)
        String source,

        @Schema(description = "When the alert was resolved. Null while pending, and for alerts resolved before the timestamp was tracked.",
                nullable = true)
        Date resolvedAt
) {
}
