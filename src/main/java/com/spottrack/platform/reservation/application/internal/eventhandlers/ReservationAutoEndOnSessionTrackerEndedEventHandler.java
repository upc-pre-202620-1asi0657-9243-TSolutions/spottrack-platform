package com.spottrack.platform.reservation.application.internal.eventhandlers;

import com.spottrack.platform.monitoring.interfaces.events.SessionTrackerEndedIntegrationEvent;
import com.spottrack.platform.reservation.application.commandServices.ReservationCommandService;
import com.spottrack.platform.reservation.application.queryservices.ReservationQueryService;
import com.spottrack.platform.reservation.domain.model.commands.EndReservation;
import com.spottrack.platform.reservation.domain.model.queries.GetReservationByUuidQuery;
import com.spottrack.platform.reservation.domain.model.valueobjects.ReservationId;
import com.spottrack.platform.reservation.domain.model.valueobjects.ReservationStatus;
import com.spottrack.platform.shared.application.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Once the session tracker monitoring a booked reservation ends (inactivity
 * timeout, admin manually ending the session, etc.), the reservation itself
 * is over too — end it, so it doesn't sit ACTIVE with no usage being tracked.
 */
@Slf4j
@Component
public class ReservationAutoEndOnSessionTrackerEndedEventHandler {

    private final ReservationQueryService reservationQueryService;
    private final ReservationCommandService reservationCommandService;

    public ReservationAutoEndOnSessionTrackerEndedEventHandler(ReservationQueryService reservationQueryService,
                                                                ReservationCommandService reservationCommandService) {
        this.reservationQueryService = reservationQueryService;
        this.reservationCommandService = reservationCommandService;
    }

    @EventListener
    public void on(SessionTrackerEndedIntegrationEvent event) {
        var reservationOpt = reservationQueryService.handle(new GetReservationByUuidQuery(event.reservationId()));
        if (reservationOpt.isEmpty() || reservationOpt.get().getStatus() != ReservationStatus.ACTIVE) return;

        var result = reservationCommandService.handle(new EndReservation(new ReservationId(event.reservationId())));
        if (result instanceof Result.Failure(var error)) {
            log.warn("Failed to auto-end reservation {} after its session tracker ended: {}",
                    event.reservationId(), error.message());
        }
    }
}
