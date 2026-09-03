package com.spottrack.platform.monitoring.application.internal.eventhandler;

import com.spottrack.platform.monitoring.application.commandServices.SessionTrackerCommandService;
import com.spottrack.platform.monitoring.application.queryServices.SessionTrackerQueryService;
import com.spottrack.platform.monitoring.domain.model.commands.EndUsageSessionCommand;
import com.spottrack.platform.monitoring.domain.model.queries.GetSessionTrackerByReservationIdQuery;
import com.spottrack.platform.monitoring.domain.model.valueobjects.ReservationId;
import com.spottrack.platform.reservation.interfaces.events.ReservationCancelledIntegrationEvent;
import com.spottrack.platform.reservation.interfaces.events.ReservationEndedIntegrationEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

/**
 * Once the reservation it was tracking is over (ended or cancelled), the
 * session tracker's job is done — end it, which cascades (via
 * UsageSessionEndedEventHandler) to calculating and reporting its final
 * activity, after which it's deleted (see SessionTimeCalculatedEventHandler).
 */
@Service
public class SessionTrackerAutoTerminationEventHandler {
    private final SessionTrackerQueryService sessionTrackerQueryService;
    private final SessionTrackerCommandService sessionTrackerCommandService;

    public SessionTrackerAutoTerminationEventHandler(SessionTrackerQueryService sessionTrackerQueryService, SessionTrackerCommandService sessionTrackerCommandService) {
        this.sessionTrackerQueryService = sessionTrackerQueryService;
        this.sessionTrackerCommandService = sessionTrackerCommandService;
    }

    @EventListener
    public void on(ReservationEndedIntegrationEvent event) {
        endTrackerForReservation(event.reservationId());
    }

    @EventListener
    public void on(ReservationCancelledIntegrationEvent event) {
        endTrackerForReservation(event.reservationId());
    }

    private void endTrackerForReservation(String reservationId) {
        var tracker = sessionTrackerQueryService.handle(
                new GetSessionTrackerByReservationIdQuery(new ReservationId(reservationId)));
        if (tracker.isPresent() && tracker.get().isSessionIsActive()) {
            sessionTrackerCommandService.handle(
                    new EndUsageSessionCommand(tracker.get().getSessionTrackerId()));
        }
    }
}
