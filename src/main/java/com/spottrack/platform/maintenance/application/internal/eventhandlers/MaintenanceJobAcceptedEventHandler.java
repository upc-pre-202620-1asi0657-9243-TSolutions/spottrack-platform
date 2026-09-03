package com.spottrack.platform.maintenance.application.internal.eventhandlers;

import com.spottrack.platform.maintenance.application.commandServices.MaintenanceCommandService;
import com.spottrack.platform.maintenance.domain.model.commands.AssignTechnicalTicket;
import com.spottrack.platform.maintenance.domain.model.events.MaintenanceJobAcceptedEvent;
import com.spottrack.platform.maintenance.domain.repositories.TechnicalTicketRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Policy: [MaintenanceJob Accepted] Assign Technical Ticket — a technician
 * accepting their unassigned job should also assign them to the ticket that
 * job was spawned for, instead of leaving that as a separate manual step.
 */
@Component
public class MaintenanceJobAcceptedEventHandler {

    private final TechnicalTicketRepository technicalTicketRepository;
    private final MaintenanceCommandService maintenanceCommandService;

    public MaintenanceJobAcceptedEventHandler(TechnicalTicketRepository technicalTicketRepository,
                                               MaintenanceCommandService maintenanceCommandService) {
        this.technicalTicketRepository = technicalTicketRepository;
        this.maintenanceCommandService = maintenanceCommandService;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void on(MaintenanceJobAcceptedEvent event) {
        technicalTicketRepository.findByMaintenanceId(event.maintenanceId())
                .ifPresent(ticket -> maintenanceCommandService.handle(
                        new AssignTechnicalTicket(ticket.getTicketId(), event.technicianId())));
    }
}
