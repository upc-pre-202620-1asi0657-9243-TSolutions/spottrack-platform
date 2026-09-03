package com.spottrack.platform.reservation.application.queryservices;

import com.spottrack.platform.reservation.domain.model.aggregates.Reservation;
import com.spottrack.platform.reservation.domain.model.queries.GetAllReservationsQuery;
import com.spottrack.platform.reservation.domain.model.queries.GetReservationByUuidQuery;
import com.spottrack.platform.reservation.domain.model.queries.GetReservationsByClientIdQuery;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public interface ReservationQueryService {
    List<Reservation> handle(GetAllReservationsQuery query);
    Optional<Reservation> handle(GetReservationByUuidQuery query);
    List<Reservation> handle(GetReservationsByClientIdQuery query);
}
