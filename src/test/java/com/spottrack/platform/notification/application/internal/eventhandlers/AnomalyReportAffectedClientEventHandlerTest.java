package com.spottrack.platform.notification.application.internal.eventhandlers;

import com.spottrack.platform.monitoring.interfaces.events.AnomalyReportSubmittedIntegrationEvent;
import com.spottrack.platform.notification.application.commandservices.NotificationCommandService;
import com.spottrack.platform.notification.domain.model.commands.RegisterAffectedClientCommand;
import com.spottrack.platform.reservation.interfaces.acl.ReservationContextFacade;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AnomalyReportAffectedClientEventHandlerTest {

    private ReservationContextFacade reservations;
    private NotificationCommandService service;
    private AnomalyReportAffectedClientEventHandler handler;

    @BeforeEach
    void setUp() {
        reservations = mock(ReservationContextFacade.class);
        service = mock(NotificationCommandService.class);
        handler = new AnomalyReportAffectedClientEventHandler(reservations, service);
    }

    @Test
    void registersTheClientOfTheReportedReservationAsAffected() {
        when(reservations.fetchClientIdByReservationId("res-1")).thenReturn(Optional.of(7L));

        handler.on(report("res-1"));

        verify(service).handle(new RegisterAffectedClientCommand(7L, "eq-1"));
    }

    @Test
    void skipsReportsWhoseReservationIsUnknown() {
        when(reservations.fetchClientIdByReservationId("res-x")).thenReturn(Optional.empty());

        handler.on(report("res-x"));

        verify(service, never()).handle(any(RegisterAffectedClientCommand.class));
    }

    private static AnomalyReportSubmittedIntegrationEvent report(String reservationId) {
        return new AnomalyReportSubmittedIntegrationEvent(reservationId, "eq-1", "zone-1", "Broken cable", LocalDate.now());
    }
}
