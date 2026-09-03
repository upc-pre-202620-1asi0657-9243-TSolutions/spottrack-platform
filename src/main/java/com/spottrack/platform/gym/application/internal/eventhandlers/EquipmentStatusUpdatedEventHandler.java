package com.spottrack.platform.gym.application.internal.eventhandlers;

import com.spottrack.platform.gym.domain.model.events.EquipmentStatusUpdatedEvent;
import com.spottrack.platform.gym.interfaces.events.EquipmentStatusUpdatedIntegrationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

/**
 * Translates the internal {@link EquipmentStatusUpdatedEvent} domain event into a
 * {@link EquipmentStatusUpdatedIntegrationEvent} and re-publishes it for cross-context consumers.
 */
@Service
public class EquipmentStatusUpdatedEventHandler {

    private final ApplicationEventPublisher eventPublisher;

    public EquipmentStatusUpdatedEventHandler(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    @EventListener
    public void on(EquipmentStatusUpdatedEvent event) {
        eventPublisher.publishEvent(
                new EquipmentStatusUpdatedIntegrationEvent(event.equipmentId(), event.status()));
    }
}
