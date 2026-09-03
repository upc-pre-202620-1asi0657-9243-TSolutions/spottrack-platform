package com.spottrack.platform.maintenance.application.internal.eventhandlers;

import com.spottrack.platform.maintenance.domain.model.events.TechnicalTicketAssignedEvent;
import com.spottrack.platform.maintenance.interfaces.events.EquipmentUnderMaintenanceIntegrationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class TechnicalTicketAssignedEventHandler {

    private final ApplicationEventPublisher eventPublisher;

    public TechnicalTicketAssignedEventHandler(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(TechnicalTicketAssignedEvent event) {
        eventPublisher.publishEvent(
                new EquipmentUnderMaintenanceIntegrationEvent(event.equipmentId(), event.ticketId())
        );
    }
}
