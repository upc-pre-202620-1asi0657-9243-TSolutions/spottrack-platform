package com.spottrack.platform.reservation.application.internal.eventhandlers;

import com.spottrack.platform.reservation.domain.model.events.RequestOccupyEquipmentSubmittedEvent;
import com.spottrack.platform.reservation.interfaces.events.RequestOccupyEquipmentSubmittedIntegrationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

/**
 * Translates the internal {@link RequestOccupyEquipmentSubmittedEvent} domain event
 * into a {@link RequestOccupyEquipmentSubmittedIntegrationEvent} and re-publishes it
 * on the Spring event bus for cross-context consumers (e.g. {@code gym}).
 */
@Service("reservationRequestOccupyEquipmentSubmittedEventHandler")
public class RequestOccupyEquipmentSubmittedEventHandler {

    private final ApplicationEventPublisher eventPublisher;

    public RequestOccupyEquipmentSubmittedEventHandler(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    @EventListener
    public void on(RequestOccupyEquipmentSubmittedEvent event) {
        eventPublisher.publishEvent(new RequestOccupyEquipmentSubmittedIntegrationEvent
                (event.requestId(), event.equipmentId()));
    }
}
