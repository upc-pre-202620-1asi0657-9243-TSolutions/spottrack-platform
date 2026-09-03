package com.spottrack.platform.reservation.domain.model.queries;

public record GetReservationRequestByUuidQuery(String uuid) {
    public GetReservationRequestByUuidQuery {
        if (uuid == null || uuid.isBlank()) {
            throw new IllegalArgumentException("Reservation request uuid must not be null or blank");
        }
    }
}
