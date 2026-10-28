package com.spottrack.platform.reservation.application.internal.eventhandlers;

import com.spottrack.platform.reservation.domain.model.events.ReservationExpiredEvent;
import com.spottrack.platform.reservation.interfaces.events.ReservationExpiredIntegrationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Translates the internal {@link ReservationExpiredEvent} into a
 * {@link ReservationExpiredIntegrationEvent} for cross-context consumers.
 */
@Component
public class ReservationExpiredEventHandler {

    private final ApplicationEventPublisher eventPublisher;

    public ReservationExpiredEventHandler(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    @EventListener
    public void on(ReservationExpiredEvent event) {
        eventPublisher.publishEvent(new ReservationExpiredIntegrationEvent(
                event.reservationId(), event.equipmentId(), event.clientId()));
    }
}
