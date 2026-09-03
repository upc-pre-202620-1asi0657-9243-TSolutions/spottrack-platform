package com.spottrack.platform.reservation.application.internal.queryserivces;

import com.spottrack.platform.reservation.application.queryservices.ReservationRequestQueryService;
import com.spottrack.platform.reservation.domain.model.aggregates.ReservationRequest;
import com.spottrack.platform.reservation.domain.model.queries.GetReservationRequestByUuidQuery;
import com.spottrack.platform.reservation.domain.repositories.ReservationRequestRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ReservationRequestQueryServiceImpl implements ReservationRequestQueryService {

    private final ReservationRequestRepository reservationRequestRepository;

    public ReservationRequestQueryServiceImpl(ReservationRequestRepository reservationRequestRepository) {
        this.reservationRequestRepository = reservationRequestRepository;
    }

    @Override
    public Optional<ReservationRequest> handle(GetReservationRequestByUuidQuery query) {
        return reservationRequestRepository.findByUuid(query.uuid());
    }
}
