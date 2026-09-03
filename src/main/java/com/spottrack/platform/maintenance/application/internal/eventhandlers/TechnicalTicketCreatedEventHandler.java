package com.spottrack.platform.maintenance.application.internal.eventhandlers;

import com.spottrack.platform.maintenance.application.commandServices.MaintenanceCommandService;
import com.spottrack.platform.maintenance.domain.model.commands.CreateMaintenanceJob;
import com.spottrack.platform.maintenance.domain.model.events.TechnicalTicketCreatedEvent;
import com.spottrack.platform.maintenance.interfaces.events.EquipmentOutOfServiceIntegrationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class TechnicalTicketCreatedEventHandler {

    private final ApplicationEventPublisher eventPublisher;
    private final MaintenanceCommandService maintenanceCommandService;

    public TechnicalTicketCreatedEventHandler(ApplicationEventPublisher eventPublisher,
                                              MaintenanceCommandService maintenanceCommandService) {
        this.eventPublisher = eventPublisher;
        this.maintenanceCommandService = maintenanceCommandService;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void on(TechnicalTicketCreatedEvent event) {
        eventPublisher.publishEvent(
                new EquipmentOutOfServiceIntegrationEvent(event.equipmentId(), event.ticketId(), event.maintenanceId())
        );

        // Policy: [Technical Ticket Created] spawn an unassigned MaintenanceJob
        // so a Technician has something to accept (board step 3).
        maintenanceCommandService.handle(new CreateMaintenanceJob(event.maintenanceId()));
    }
}
