package com.spottrack.platform.monitoring.application.internal.eventhandler;

import com.spottrack.platform.monitoring.domain.model.events.MotionSensorDisconnectedEvent;
import com.spottrack.platform.monitoring.interfaces.events.MotionSensorDisconnectedIntegrationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

/**
 * Translates the internal disconnection signal into this context's public
 * integration event, per the "Domain → Integration Event" policy.
 */
@Service
public class MotionSensorDisconnectedEventHandler {
    private final ApplicationEventPublisher eventPublisher;

    public MotionSensorDisconnectedEventHandler(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    @EventListener
    public void on(MotionSensorDisconnectedEvent event) {
        eventPublisher.publishEvent(new MotionSensorDisconnectedIntegrationEvent(
                event.motionSensorId().uuid(), event.equipmentId().uuid()));
    }
}
