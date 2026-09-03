package com.spottrack.platform.maintenance.domain.model.aggregates;

import com.spottrack.platform.maintenance.domain.model.commands.RegisterMaintenanceCompletion;
import com.spottrack.platform.maintenance.domain.model.events.MaintenanceCompletionRegisteredToLogEvent;
import com.spottrack.platform.maintenance.domain.model.valueobjects.MaintenanceLogId;
import com.spottrack.platform.shared.domain.model.aggregates.AbstractDomainAggregateRoot;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class MaintenanceLog extends AbstractDomainAggregateRoot<MaintenanceLog> {

    private MaintenanceLogId logId;
    private String ticketId;
    private String maintenanceId;
    private String notes;
    private BigDecimal cost;
    private LocalDateTime completedAt;

    protected MaintenanceLog() {}

    public MaintenanceLog(RegisterMaintenanceCompletion command) {
        this.logId = new MaintenanceLogId(UUID.randomUUID().toString());
        this.ticketId = command.ticketId().uuid();
        this.maintenanceId = command.maintenanceId().uuid();
        this.notes = command.notes();
        this.cost = command.cost();
        this.completedAt = LocalDateTime.now();
        registerDomainEvent(new MaintenanceCompletionRegisteredToLogEvent(this.logId.uuid(), this.ticketId, this.maintenanceId));
    }

    public MaintenanceLog(String logId, String ticketId, String maintenanceId, String notes, BigDecimal cost, LocalDateTime completedAt) {
        this.logId = new MaintenanceLogId(logId);
        this.ticketId = ticketId;
        this.maintenanceId = maintenanceId;
        this.notes = notes;
        this.cost = cost;
        this.completedAt = completedAt;
    }
}
