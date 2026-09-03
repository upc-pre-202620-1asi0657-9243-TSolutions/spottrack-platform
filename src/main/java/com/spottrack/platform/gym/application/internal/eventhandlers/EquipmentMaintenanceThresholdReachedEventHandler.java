package com.spottrack.platform.gym.application.internal.eventhandlers;

import com.spottrack.platform.gym.domain.model.events.MaintenanceThresholdReachedEvent;
import com.spottrack.platform.gym.interfaces.events.MaintenanceThresholdReachedIntegrationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Policy: [Threshold reached?] Emit Maintenance Request (per the gym event-storming board).
 */
@Component
public class EquipmentMaintenanceThresholdReachedEventHandler {

    private final ApplicationEventPublisher eventPublisher;

    public EquipmentMaintenanceThresholdReachedEventHandler(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(MaintenanceThresholdReachedEvent event) {
        eventPublisher.publishEvent(
                new MaintenanceThresholdReachedIntegrationEvent(event.equipmentId(), event.threshold()));
    }
}
