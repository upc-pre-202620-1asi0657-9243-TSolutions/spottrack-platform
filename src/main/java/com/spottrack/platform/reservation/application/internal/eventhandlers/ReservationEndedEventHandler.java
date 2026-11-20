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
 * Policy: [Reservation Ended] Release the equipment
 *
 * Fires when a reservation ends (either by timer expiry or client explicitly ending it).
 * It republishes a {@link ReservationEndedIntegrationEvent} so other contexts (e.g. monitoring)
 * can react without depending on the internal domain event, and requests the equipment to be
 * marked AVAILABLE.
 *
 * The clients waiting for the machine are not notified from here: the notification context
 * reacts to the resulting equipment status change, which also covers releases that do not come
 * from a reservation (sensor sessions, manual updates).
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
        log.info("Policy fired — reservation {} ended for client {}, equipment {} is now free",
                event.reservationId(), event.clientId(), event.equipmentId());

        eventPublisher.publishEvent(new ReservationEndedIntegrationEvent(
                event.reservationId(), event.equipmentId(), event.clientId()));

        eventPublisher.publishEvent(new EquipmentStatusChangeToAvailableRequestedIntegrationEvent(
                event.reservationId(), event.equipmentId(), "AVAILABLE"));
    }
}
