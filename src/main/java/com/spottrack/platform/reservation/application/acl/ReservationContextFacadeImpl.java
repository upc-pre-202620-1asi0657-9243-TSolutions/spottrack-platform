package com.spottrack.platform.reservation.application.acl;

import com.spottrack.platform.reservation.application.queryservices.ReservationQueryService;
import com.spottrack.platform.reservation.domain.model.queries.GetReservationByUuidQuery;
import com.spottrack.platform.reservation.interfaces.acl.ReservationContextFacade;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ReservationContextFacadeImpl implements ReservationContextFacade {

    private final ReservationQueryService reservationQueryService;

    public ReservationContextFacadeImpl(ReservationQueryService reservationQueryService) {
        this.reservationQueryService = reservationQueryService;
    }

    @Override
    public Optional<Long> fetchClientIdByReservationId(String reservationId) {
        return reservationQueryService.handle(new GetReservationByUuidQuery(reservationId))
                .map(reservation -> reservation.getClientId().clientId());
    }
}
