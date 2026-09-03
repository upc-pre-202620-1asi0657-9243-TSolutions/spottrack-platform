package com.spottrack.platform.monitoring.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;

public record ReportAnomalyCommandResource(
        @Schema
        String reservationId,
        @Schema
        String equipmentId,
        @Schema
        String zoneId,
        @Schema
        String anomalyDescription) {
}
