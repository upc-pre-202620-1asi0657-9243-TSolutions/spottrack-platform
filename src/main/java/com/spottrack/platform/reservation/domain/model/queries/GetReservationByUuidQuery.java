package com.spottrack.platform.reservation.domain.model.queries;

public record GetReservationByUuidQuery(String uuid) {
    public GetReservationByUuidQuery {
        if (uuid == null || uuid.isBlank()) {
            throw new IllegalArgumentException("Reservation uuid must not be null or blank");
        }
    }
}
