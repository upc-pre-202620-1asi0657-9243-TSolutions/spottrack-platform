package com.spottrack.platform.reservation.interfaces.rest.resources;

/**
 * Request body for POST /api/v1/reservations/reserve.
 * clientId is resolved from the JWT.
 * startedAt and timeExpiry are set server-side by the aggregate.
 */
public record InitiateExpressReservationResource(
        String equipmentId,
        String startTime,
        String endTime
) {}
