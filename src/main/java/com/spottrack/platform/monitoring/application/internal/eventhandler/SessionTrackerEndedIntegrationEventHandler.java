package com.spottrack.platform.monitoring.application.internal.eventhandler;

import com.spottrack.platform.monitoring.domain.model.events.UsageSessionEndedEvent;
import com.spottrack.platform.monitoring.interfaces.events.SessionTrackerEndedIntegrationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Translates the internal {@link UsageSessionEndedEvent} into a cross-context
 * integration event so the {@code reservation} bounded context can end the
 * linked reservation too — only published when the session was actually
 * tracking a booked reservation (walk-up sessions have none).
 */
@Component
public class SessionTrackerEndedIntegrationEventHandler {

    private final ApplicationEventPublisher eventPublisher;

    public SessionTrackerEndedIntegrationEventHandler(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    @EventListener
    public void on(UsageSessionEndedEvent event) {
        if (event.reservationId() == null) return;
        eventPublisher.publishEvent(new SessionTrackerEndedIntegrationEvent(
                event.sessionTrackerId().uuid(), event.reservationId()));
    }
}
