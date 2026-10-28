package com.spottrack.platform.monitoring.application.internal.eventhandler;

import com.spottrack.platform.monitoring.application.commandServices.SessionTrackerCommandService;
import com.spottrack.platform.monitoring.application.queryServices.SessionTrackerQueryService;
import com.spottrack.platform.monitoring.domain.model.aggregates.SessionTracker;
import com.spottrack.platform.monitoring.domain.model.commands.EndUsageSessionCommand;
import com.spottrack.platform.monitoring.domain.model.queries.GetSessionTrackerByReservationIdQuery;
import com.spottrack.platform.monitoring.domain.model.valueobjects.ReservationId;
import com.spottrack.platform.monitoring.domain.model.valueobjects.SessionTrackerId;
import com.spottrack.platform.reservation.interfaces.events.ReservationExpiredIntegrationEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SessionTrackerAutoTerminationEventHandlerTest {

    private static final ReservationExpiredIntegrationEvent EVENT =
            new ReservationExpiredIntegrationEvent("r-1", "eq-1", 5L);
    private static final GetSessionTrackerByReservationIdQuery QUERY =
            new GetSessionTrackerByReservationIdQuery(new ReservationId("r-1"));

    private SessionTrackerQueryService queryService;
    private SessionTrackerCommandService commandService;
    private SessionTracker tracker;
    private SessionTrackerAutoTerminationEventHandler handler;

    @BeforeEach
    void setUp() {
        queryService = mock(SessionTrackerQueryService.class);
        commandService = mock(SessionTrackerCommandService.class);
        tracker = mock(SessionTracker.class);
        handler = new SessionTrackerAutoTerminationEventHandler(queryService, commandService);
        when(tracker.getSessionTrackerId()).thenReturn(new SessionTrackerId("t-1"));
    }

    @Test
    void endsTheActiveTrackerOfAnExpiredReservation() {
        when(tracker.isSessionIsActive()).thenReturn(true);
        when(queryService.handle(QUERY)).thenReturn(Optional.of(tracker));

        handler.on(EVENT);

        verify(commandService).handle(new EndUsageSessionCommand(new SessionTrackerId("t-1")));
    }

    @Test
    void doesNothingWhenTheTrackerIsAlreadyInactive() {
        when(tracker.isSessionIsActive()).thenReturn(false);
        when(queryService.handle(QUERY)).thenReturn(Optional.of(tracker));

        handler.on(EVENT);

        verify(commandService, never()).handle(any(EndUsageSessionCommand.class));
    }

    @Test
    void doesNothingWhenThereIsNoTracker() {
        when(queryService.handle(QUERY)).thenReturn(Optional.empty());

        handler.on(EVENT);

        verify(commandService, never()).handle(any(EndUsageSessionCommand.class));
    }
}
