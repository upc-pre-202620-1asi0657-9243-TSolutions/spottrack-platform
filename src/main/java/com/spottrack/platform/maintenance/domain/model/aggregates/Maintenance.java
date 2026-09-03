package com.spottrack.platform.maintenance.domain.model.aggregates;

import com.spottrack.platform.maintenance.domain.model.valueobjects.EquipmentId;
import com.spottrack.platform.maintenance.domain.model.commands.RequestMaintenance;
import com.spottrack.platform.maintenance.domain.model.events.MaintenanceRequestedEvent;
import com.spottrack.platform.maintenance.domain.model.valueobjects.MaintenanceId;
import com.spottrack.platform.maintenance.domain.model.valueobjects.MaintenanceStatus;
import com.spottrack.platform.shared.domain.model.aggregates.AbstractDomainAggregateRoot;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class Maintenance extends AbstractDomainAggregateRoot<Maintenance> {

    private MaintenanceId id;

    private EquipmentId equipmentId;

    private String requestedBy;

    private String description;

    private MaintenanceStatus status;

    protected Maintenance() {}

    public Maintenance(RequestMaintenance command) {
        this.id = new MaintenanceId(UUID.randomUUID().toString());
        this.equipmentId = command.equipmentId();
        this.requestedBy = command.requestedBy();
        this.description = command.description();
        this.status = MaintenanceStatus.REQUESTED;
        registerDomainEvent(new MaintenanceRequestedEvent(this.id.uuid(), this.equipmentId, this.requestedBy));
    }

    public Maintenance(String maintenanceId, String equipmentId, String requestedBy, String description, MaintenanceStatus status) {
        this.id = new MaintenanceId(maintenanceId);
        this.equipmentId = new EquipmentId(equipmentId);
        this.requestedBy = requestedBy;
        this.description = description;
        this.status = status != null ? status : MaintenanceStatus.REQUESTED;
    }
}
