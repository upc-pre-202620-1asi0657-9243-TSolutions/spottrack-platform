package com.spottrack.platform.reservation.application.internal.eventhandlers;

import com.spottrack.platform.reservation.domain.model.events.ExpressReservationInitiatedEvent;
import com.spottrack.platform.reservation.interfaces.events.ExpressReservationInitiatedIntegrationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

/**
 * Translates the internal {@link ExpressReservationInitiatedEvent} domain event
 * into an {@link ExpressReservationInitiatedIntegrationEvent} and re-publishes it
 * on the Spring event bus for cross-context consumers (e.g. {@code monitoring},
 * to start tracking usage of the reserved equipment).
 */
@Service
public class ExpressReservationInitiatedIntegrationEventHandler {

    private final ApplicationEventPublisher eventPublisher;

    public ExpressReservationInitiatedIntegrationEventHandler(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    @EventListener
    public void on(ExpressReservationInitiatedEvent event) {
        eventPublisher.publishEvent(new ExpressReservationInitiatedIntegrationEvent(
                event.reservationId(), event.equipmentId(), event.clientId()));
    }
}
