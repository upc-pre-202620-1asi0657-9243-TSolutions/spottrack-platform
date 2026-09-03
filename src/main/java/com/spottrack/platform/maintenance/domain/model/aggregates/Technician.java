package com.spottrack.platform.maintenance.domain.model.aggregates;

import com.spottrack.platform.maintenance.domain.model.commands.CreateTechnician;
import com.spottrack.platform.maintenance.domain.model.events.TechnicianCreatedEvent;
import com.spottrack.platform.maintenance.domain.model.valueobjects.TechnicianId;
import com.spottrack.platform.shared.domain.model.aggregates.AbstractDomainAggregateRoot;
import lombok.Getter;

import java.util.UUID;

@Getter
public class Technician extends AbstractDomainAggregateRoot<Technician> {

    private TechnicianId technicianId;
    private String name;

    protected Technician() {}

    public Technician(CreateTechnician command) {
        this.technicianId = new TechnicianId(UUID.randomUUID().toString());
        this.name = command.name();
        registerDomainEvent(new TechnicianCreatedEvent(this.technicianId.uuid(), this.name));
    }

    public Technician(String technicianId, String name) {
        this.technicianId = new TechnicianId(technicianId);
        this.name = name;
    }
}
