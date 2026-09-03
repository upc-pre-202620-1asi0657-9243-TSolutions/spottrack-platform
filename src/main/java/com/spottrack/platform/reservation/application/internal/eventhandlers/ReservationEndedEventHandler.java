package com.spottrack.platform.reservation.application.internal.eventhandlers;

import com.spottrack.platform.reservation.domain.model.events.ReservationEndedEvent;
import com.spottrack.platform.reservation.interfaces.events.EquipmentStatusChangeToAvailableRequestedIntegrationEvent;
import com.spottrack.platform.reservation.interfaces.events.ReservationEndedIntegrationEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Policy: [Reservation Ended] Send Notification to Alerts
 *
 * Fires when a reservation ends (either by timer expiry or client explicitly ending it).
 * The Alerts bounded context should be notified so it can inform the client,
 * update dashboards, or log the session for reporting. It also republishes a
 * {@link ReservationEndedIntegrationEvent} so other contexts (e.g. monitoring) can
 * react without depending on the internal domain event.
 *
 * The Alerts notification is stubbed — in a real system this would call the
 * Alerts service or publish a message.
 */
@Component
public class ReservationEndedEventHandler {

    private static final Logger log = LoggerFactory.getLogger(ReservationEndedEventHandler.class);

    private final ApplicationEventPublisher eventPublisher;

    public ReservationEndedEventHandler(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    @EventListener
    public void on(ReservationEndedEvent event) {
        // TODO: notify Alerts bounded context that the reservation ended
        // e.g. alertsAclService.notifyReservationEnded(event.reservationId(), event.clientId())
        log.info("Policy fired — reservation {} ended for client {}, equipment {} is now free",
                event.reservationId(), event.clientId(), event.equipmentId());

        eventPublisher.publishEvent(new ReservationEndedIntegrationEvent(
                event.reservationId(), event.equipmentId(), event.clientId()));

        eventPublisher.publishEvent(new EquipmentStatusChangeToAvailableRequestedIntegrationEvent(
                event.reservationId(), event.equipmentId(), "AVAILABLE"));
    }
}
