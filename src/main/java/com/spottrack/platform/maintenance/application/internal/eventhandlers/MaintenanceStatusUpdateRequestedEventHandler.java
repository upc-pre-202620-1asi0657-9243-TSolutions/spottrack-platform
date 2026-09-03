package com.spottrack.platform.maintenance.application.internal.eventhandlers;

import com.spottrack.platform.maintenance.application.commandServices.MaintenanceCommandService;
import com.spottrack.platform.maintenance.domain.model.commands.CompleteMaintenance;
import com.spottrack.platform.maintenance.domain.model.events.MaintenanceStatusUpdateRequestedEvent;
import com.spottrack.platform.maintenance.domain.model.valueobjects.MaintenanceStatus;
import com.spottrack.platform.maintenance.domain.model.valueobjects.TechnicalTicketId;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class MaintenanceStatusUpdateRequestedEventHandler {

    private final MaintenanceCommandService maintenanceCommandService;

    public MaintenanceStatusUpdateRequestedEventHandler(MaintenanceCommandService maintenanceCommandService) {
        this.maintenanceCommandService = maintenanceCommandService;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void on(MaintenanceStatusUpdateRequestedEvent event) {
        if (MaintenanceStatus.COMPLETED.name().equals(event.requestedStatus())) {
            maintenanceCommandService.handle(new CompleteMaintenance(new TechnicalTicketId(event.ticketId())));
        }
    }
}
