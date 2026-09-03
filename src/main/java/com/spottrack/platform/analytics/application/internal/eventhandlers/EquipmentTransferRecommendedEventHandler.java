package com.spottrack.platform.analytics.application.internal.eventhandlers;

import com.spottrack.platform.analytics.domain.model.events.EquipmentTransferRecommendedEvent;
import com.spottrack.platform.analytics.interfaces.events.EquipmentRelocationRequestedIntegrationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Implements the "(Transfer recommended?) Request equipment relocation"
 * policy from the event storming. This context's responsibility ends at
 * publishing the integration event below — actually acting on the relocation
 * is not this bounded context's job.
 */
@Component
public class EquipmentTransferRecommendedEventHandler {

    private final ApplicationEventPublisher eventPublisher;

    public EquipmentTransferRecommendedEventHandler(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    @EventListener
    public void on(EquipmentTransferRecommendedEvent event) {
        eventPublisher.publishEvent(new EquipmentRelocationRequestedIntegrationEvent(
                event.roiProjectionId().value(), event.detail()));
    }
}
