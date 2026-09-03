package com.spottrack.platform.reservation.application.internal.eventhandlers;

import com.spottrack.platform.reservation.domain.model.events.ReservationCancelledEvent;
import com.spottrack.platform.reservation.interfaces.events.EquipmentStatusChangeToAvailableRequestedIntegrationEvent;
import com.spottrack.platform.reservation.interfaces.events.ReservationCancelledIntegrationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

/**
 * Translates the internal {@link ReservationCancelledEvent} domain event into a
 * {@link ReservationCancelledIntegrationEvent} and re-publishes it on the Spring
 * event bus for cross-context consumers (e.g. {@code monitoring}, to stop usage
 * tracking for the reserved equipment).
 */
@Service
public class ReservationCancelledEventHandler {

    private final ApplicationEventPublisher eventPublisher;

    public ReservationCancelledEventHandler(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    @EventListener
    public void on(ReservationCancelledEvent event) {
        eventPublisher.publishEvent(new ReservationCancelledIntegrationEvent(
                event.reservationId(), event.equipmentId(), event.clientId()));

        eventPublisher.publishEvent(new EquipmentStatusChangeToAvailableRequestedIntegrationEvent(
                event.reservationId(), event.equipmentId(), "AVAILABLE"));
    }
}
