package com.spottrack.platform.reservation.application.internal.queryserivces;


import com.spottrack.platform.reservation.application.queryservices.ReservationQueryService;
import com.spottrack.platform.reservation.domain.model.aggregates.Reservation;
import com.spottrack.platform.reservation.domain.model.queries.GetAllReservationsQuery;
import com.spottrack.platform.reservation.domain.model.queries.GetReservationByUuidQuery;
import com.spottrack.platform.reservation.domain.model.queries.GetReservationsByClientIdQuery;
import com.spottrack.platform.reservation.domain.repositories.ReservationRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ReservationQueryServiceImpl implements ReservationQueryService {
    private final ReservationRepository reservationRepository;

    public ReservationQueryServiceImpl(ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    @Override
    public List<Reservation> handle(GetAllReservationsQuery query) {
        return reservationRepository.findAll();
    }

    @Override
    public Optional<Reservation> handle(GetReservationByUuidQuery query) {
        return reservationRepository.findByUuid(query.uuid());
    }

    @Override
    public List<Reservation> handle(GetReservationsByClientIdQuery query) {
        return reservationRepository.findByClientId(query.clientId());
    }
}
