package com.spottrack.platform.reservation.domain.model.queries;

public record GetReservationsByClientIdQuery(Long clientId) {
    public GetReservationsByClientIdQuery {
        if (clientId == null || clientId < 1) {
            throw new IllegalArgumentException("Client id must not be null or less than 1");
        }
    }
}
