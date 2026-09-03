package com.spottrack.platform.maintenance.domain.model.aggregates;

import com.spottrack.platform.maintenance.domain.model.commands.AssignTechnicalTicket;
import com.spottrack.platform.maintenance.domain.model.commands.ModifyTicketStatus;
import com.spottrack.platform.maintenance.domain.model.commands.RequestUpdateMaintenanceStatus;
import com.spottrack.platform.maintenance.domain.model.commands.UpdateMaintenanceStatus;
import com.spottrack.platform.maintenance.domain.model.events.MaintenanceStatusUpdateRequestedEvent;
import com.spottrack.platform.maintenance.domain.model.events.MaintenanceStatusUpdatedEvent;
import com.spottrack.platform.maintenance.domain.model.events.TechnicalTicketAssignedEvent;
import com.spottrack.platform.maintenance.domain.model.events.TechnicalTicketCreatedEvent;
import com.spottrack.platform.maintenance.domain.model.events.TicketStatusMarkedAsResolvedEvent;
import com.spottrack.platform.maintenance.domain.model.events.TicketStatusModifiedEvent;
import com.spottrack.platform.maintenance.domain.model.valueobjects.MaintenanceStatus;
import com.spottrack.platform.maintenance.domain.model.valueobjects.TechnicalTicketId;
import com.spottrack.platform.maintenance.domain.model.valueobjects.TicketPriority;
import com.spottrack.platform.maintenance.domain.model.valueobjects.TicketStatus;
import com.spottrack.platform.maintenance.domain.model.valueobjects.TicketType;
import com.spottrack.platform.shared.domain.model.aggregates.AbstractDomainAggregateRoot;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class TechnicalTicket extends AbstractDomainAggregateRoot<TechnicalTicket> {

    private TechnicalTicketId ticketId;
    private String maintenanceId;
    private String equipmentId;
    private String technicianId;
    private String description;
    private TicketPriority priority;
    private TicketType type;
    private TicketStatus ticketStatus;
    private MaintenanceStatus maintenanceStatus;
    private LocalDateTime createdAt;

    protected TechnicalTicket() {}

    public TechnicalTicket(String maintenanceId, String equipmentId, String description,
                           TicketPriority priority, TicketType type) {
        this.ticketId = new TechnicalTicketId(UUID.randomUUID().toString());
        this.maintenanceId = maintenanceId;
        this.equipmentId = equipmentId;
        this.description = description;
        this.priority = priority;
        this.type = type;
        this.ticketStatus = TicketStatus.OPEN;
        this.maintenanceStatus = MaintenanceStatus.REQUESTED;
        registerDomainEvent(new TechnicalTicketCreatedEvent(this.ticketId.uuid(), this.maintenanceId, this.equipmentId));
    }

    public TechnicalTicket(String ticketId, String maintenanceId, String equipmentId,
                           String technicianId, String description, TicketPriority priority, TicketType type,
                           String ticketStatus, String maintenanceStatus, LocalDateTime createdAt) {
        this.ticketId = new TechnicalTicketId(ticketId);
        this.maintenanceId = maintenanceId;
        this.equipmentId = equipmentId;
        this.technicianId = technicianId;
        this.description = description;
        this.priority = priority;
        this.type = type;
        this.ticketStatus = TicketStatus.valueOf(ticketStatus);
        this.maintenanceStatus = MaintenanceStatus.valueOf(maintenanceStatus);
        this.createdAt = createdAt;
    }

    public void assign(AssignTechnicalTicket command) {
        if (this.ticketStatus == TicketStatus.IN_PROGRESS) {
            throw new IllegalStateException("maintenance.error.ticket.alreadyAssigned");
        }
        this.technicianId = command.technicianId();
        this.ticketStatus = TicketStatus.IN_PROGRESS;
        registerDomainEvent(new TechnicalTicketAssignedEvent(this.ticketId.uuid(), this.technicianId, this.equipmentId));
    }

    public void markAsResolved() {
        if (this.ticketStatus == TicketStatus.RESOLVED) {
            throw new IllegalStateException("maintenance.error.ticket.alreadyResolved");
        }
        this.ticketStatus = TicketStatus.RESOLVED;
        this.maintenanceStatus = MaintenanceStatus.COMPLETED;
        registerDomainEvent(new TicketStatusMarkedAsResolvedEvent(this.ticketId.uuid(), this.equipmentId));
    }

    public void modifyStatus(ModifyTicketStatus command) {
        if (command.newStatus() == TicketStatus.RESOLVED) {
            markAsResolved();
            return;
        }
        this.ticketStatus = command.newStatus();
        registerDomainEvent(new TicketStatusModifiedEvent(this.ticketId.uuid(), command.newStatus().name()));
    }

    public void requestStatusUpdate(RequestUpdateMaintenanceStatus command) {
        registerDomainEvent(new MaintenanceStatusUpdateRequestedEvent(this.ticketId.uuid(), command.newStatus().name()));
    }

    public void updateMaintenanceStatus(UpdateMaintenanceStatus command) {
        this.maintenanceStatus = command.newStatus();
        registerDomainEvent(new MaintenanceStatusUpdatedEvent(this.ticketId.uuid(), command.newStatus().name()));
    }
}
