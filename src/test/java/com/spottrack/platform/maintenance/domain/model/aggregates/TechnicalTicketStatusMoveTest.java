package com.spottrack.platform.maintenance.domain.model.aggregates;

import com.spottrack.platform.maintenance.domain.model.commands.ModifyTicketStatus;
import com.spottrack.platform.maintenance.domain.model.commands.UpdateTechnicalTicket;
import com.spottrack.platform.maintenance.domain.model.events.TechnicalTicketAssignedEvent;
import com.spottrack.platform.maintenance.domain.model.valueobjects.TicketPriority;
import com.spottrack.platform.maintenance.domain.model.valueobjects.TicketStatus;
import com.spottrack.platform.maintenance.domain.model.valueobjects.TicketType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TechnicalTicketStatusMoveTest {

    private TechnicalTicket ticket(String technicianId) {
        var ticket = new TechnicalTicket("maintenance-1", "equipment-1", "Belt slips", TicketPriority.LOW, TicketType.CORRECTIVE);
        if (technicianId != null) {
            ticket.update(new UpdateTechnicalTicket(ticket.getTicketId(), "Belt slips", TicketPriority.LOW,
                    TicketType.CORRECTIVE, technicianId));
        }
        ticket.clearDomainEvents();
        return ticket;
    }

    private void move(TechnicalTicket ticket, TicketStatus status) {
        ticket.modifyStatus(new ModifyTicketStatus(ticket.getTicketId(), status));
    }

    @Test
    void startingWorkPutsTheEquipmentUnderMaintenance() {
        var ticket = ticket("tech-1");

        move(ticket, TicketStatus.IN_PROGRESS);

        assertEquals(TicketStatus.IN_PROGRESS, ticket.getTicketStatus());
        assertTrue(ticket.domainEvents().stream().anyMatch(e -> e instanceof TechnicalTicketAssignedEvent));
    }

    @Test
    void workCannotStartWithoutATechnician() {
        var ticket = ticket(null);

        var error = assertThrows(IllegalStateException.class, () -> move(ticket, TicketStatus.IN_PROGRESS));

        assertEquals("maintenance.error.ticket.technicianRequired", error.getMessage());
        assertEquals(TicketStatus.OPEN, ticket.getTicketStatus());
    }

    @Test
    void anInProgressTicketCanGoBackToPending() {
        var ticket = ticket("tech-1");
        move(ticket, TicketStatus.IN_PROGRESS);

        move(ticket, TicketStatus.OPEN);

        assertEquals(TicketStatus.OPEN, ticket.getTicketStatus());
    }

    @Test
    void aResolvedTicketCannotBeMoved() {
        var ticket = ticket("tech-1");
        move(ticket, TicketStatus.RESOLVED);

        var error = assertThrows(IllegalStateException.class, () -> move(ticket, TicketStatus.OPEN));

        assertEquals("maintenance.error.ticket.resolvedIsFinal", error.getMessage());
    }
}
