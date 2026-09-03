package com.spottrack.platform.reservation.interfaces.rest.resources;

/**
 * Request body for PATCH /api/v1/reservation-requests/{id}/alternative
 * reason is optional — the client may or may not explain why they want a different piece of equipment.
 */
public record RequestAlternativeEquipmentResource(String reason) {
}
