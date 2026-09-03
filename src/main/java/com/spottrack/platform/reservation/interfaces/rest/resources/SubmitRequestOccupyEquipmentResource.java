package com.spottrack.platform.reservation.interfaces.rest.resources;

/**
 * Request body for POST /api/v1/reservation-requests.
 * clientId is resolved from the JWT — it is not accepted from the request body.
 */
public record SubmitRequestOccupyEquipmentResource(String equipmentId) {
}
