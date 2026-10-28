package com.spottrack.platform.reservation.interfaces.rest.resources;

/**
 * Request body for POST /api/v1/reservations/reserve.
 * clientId is resolved from the JWT. startedAt, holdExpiresAt and timerExpiry are set server-side.
 * startTime and endTime (HH:mm or HH:mm:ss) are optional: if sent they are stored as given,
 * if missing they are derived. The express hold does not depend on them.
 */
public record InitiateExpressReservationResource(
        String equipmentId,
        String startTime,
        String endTime
) {}
