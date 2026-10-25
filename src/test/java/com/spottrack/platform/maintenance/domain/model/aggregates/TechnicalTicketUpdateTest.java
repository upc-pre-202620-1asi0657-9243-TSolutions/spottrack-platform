package com.spottrack.platform.maintenance.domain.model.aggregates;

import com.spottrack.platform.maintenance.domain.model.commands.AssignTechnicalTicket;
import com.spottrack.platform.maintenance.domain.model.commands.UpdateTechnicalTicket;
import com.spottrack.platform.maintenance.domain.model.valueobjects.TicketPriority;
import com.spottrack.platform.maintenance.domain.model.valueobjects.TicketStatus;
import com.spottrack.platform.maintenance.domain.model.valueobjects.TicketType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TechnicalTicketUpdateTest {

    private TechnicalTicket openTicket() {
        return new TechnicalTicket("maintenance-1", "equipment-1", "Belt slips", TicketPriority.LOW, TicketType.CORRECTIVE);
    }

    private UpdateTechnicalTicket update(TechnicalTicket ticket, String technicianId) {
        return new UpdateTechnicalTicket(ticket.getTicketId(), "  Belt slips under load ", TicketPriority.URGENT,
                TicketType.PREVENTIVE, technicianId);
    }

    @Test
    void editsDescriptionPriorityAndType() {
        var ticket = openTicket();

        ticket.update(update(ticket, null));

        assertEquals("Belt slips under load", ticket.getDescription());
        assertEquals(TicketPriority.URGENT, ticket.getPriority());
        assertEquals(TicketType.PREVENTIVE, ticket.getType());
        assertEquals(TicketStatus.OPEN, ticket.getTicketStatus());
    }

    @Test
    void reassignsAnInProgressTicket() {
        var ticket = openTicket();
        ticket.assign(new AssignTechnicalTicket(ticket.getTicketId(), "tech-1"));

        ticket.update(update(ticket, "tech-2"));

        assertEquals("tech-2", ticket.getTechnicianId());
        assertEquals(TicketStatus.IN_PROGRESS, ticket.getTicketStatus());
    }

    @Test
    void refusesToAssignAnOpenTicketThroughAnEdit() {
        var ticket = openTicket();

        var error = assertThrows(IllegalStateException.class, () -> ticket.update(update(ticket, "tech-1")));

        assertEquals("maintenance.error.ticket.reassignRequiresInProgress", error.getMessage());
    }

    @Test
    void refusesToEditAResolvedTicket() {
        var ticket = openTicket();
        ticket.markAsResolved();

        var error = assertThrows(IllegalStateException.class, () -> ticket.update(update(ticket, null)));

        assertEquals("maintenance.error.ticket.resolvedIsFinal", error.getMessage());
        assertEquals("Belt slips", ticket.getDescription());
    }
}
