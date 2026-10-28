package com.spottrack.platform.reservation.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Request body for POST /api/v1/reservations/reserve.
 * clientId is resolved from the JWT. startedAt, holdExpiresAt and timerExpiry are set server-side.
 * startTime and endTime (HH:mm or HH:mm:ss) are optional: if sent they are stored as given,
 * if missing they are derived. The express hold does not depend on them.
 */
public record InitiateExpressReservationResource(
        @Schema(description = "Equipment to hold", requiredMode = Schema.RequiredMode.REQUIRED)
        String equipmentId,
        @Schema(description = "Optional usage window start (HH:mm or HH:mm:ss)", example = "18:00", nullable = true)
        String startTime,
        @Schema(description = "Optional usage window end (HH:mm or HH:mm:ss)", example = "18:45", nullable = true)
        String endTime
) {}
