package com.spottrack.platform.maintenance.application.internal.eventhandlers;

import com.spottrack.platform.maintenance.domain.model.events.TicketStatusMarkedAsResolvedEvent;
import com.spottrack.platform.maintenance.interfaces.events.EquipmentStatusAvailableIntegrationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class TicketStatusMarkedAsResolvedEventHandler {

    private final ApplicationEventPublisher eventPublisher;

    public TicketStatusMarkedAsResolvedEventHandler(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(TicketStatusMarkedAsResolvedEvent event) {
        eventPublisher.publishEvent(
                new EquipmentStatusAvailableIntegrationEvent(event.equipmentId(), event.ticketId())
        );



    }
}
