package com.spottrack.platform.notification.application.internal.eventhandlers;

import com.spottrack.platform.monitoring.interfaces.events.AnomalyReportSubmittedIntegrationEvent;
import com.spottrack.platform.notification.application.commandservices.NotificationCommandService;
import com.spottrack.platform.notification.domain.model.commands.RegisterAffectedClientCommand;
import com.spottrack.platform.reservation.interfaces.acl.ReservationContextFacade;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Policy: [Anomaly Report Submitted] Register the reporting client as affected by the failure,
 * so they are notified when the equipment is operational again (US23).
 */
@Slf4j
@Component
public class AnomalyReportAffectedClientEventHandler {

    private final ReservationContextFacade reservationContextFacade;
    private final NotificationCommandService notificationCommandService;

    public AnomalyReportAffectedClientEventHandler(ReservationContextFacade reservationContextFacade,
                                                   NotificationCommandService notificationCommandService) {
        this.reservationContextFacade = reservationContextFacade;
        this.notificationCommandService = notificationCommandService;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void on(AnomalyReportSubmittedIntegrationEvent event) {
        if (event.reservationId() == null || event.reservationId().isBlank()) {
            return;
        }
        var clientId = reservationContextFacade.fetchClientIdByReservationId(event.reservationId());
        if (clientId.isEmpty()) {
            log.warn("Reservation {} of the anomaly report on equipment {} was not found; no client registered as affected.",
                    event.reservationId(), event.equipmentId());
            return;
        }
        try {
            notificationCommandService.handle(new RegisterAffectedClientCommand(clientId.get(), event.equipmentId()));
        } catch (RuntimeException e) {
            log.error("Could not register client {} as affected by equipment {}: {}",
                    clientId.get(), event.equipmentId(), e.getMessage(), e);
        }
    }
}
