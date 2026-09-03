package com.spottrack.platform.reservation.application.queryservices;

import com.spottrack.platform.reservation.domain.model.aggregates.ReservationRequest;
import com.spottrack.platform.reservation.domain.model.queries.GetReservationRequestByUuidQuery;

import java.util.Optional;

public interface ReservationRequestQueryService {
    Optional<ReservationRequest> handle(GetReservationRequestByUuidQuery query);
}
