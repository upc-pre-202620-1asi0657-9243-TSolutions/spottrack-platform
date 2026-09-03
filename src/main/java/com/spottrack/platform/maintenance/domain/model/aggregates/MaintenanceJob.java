package com.spottrack.platform.maintenance.domain.model.aggregates;

import com.spottrack.platform.maintenance.domain.model.commands.AcceptMaintenance;
import com.spottrack.platform.maintenance.domain.model.events.MaintenanceJobAcceptedEvent;
import com.spottrack.platform.maintenance.domain.model.valueobjects.MaintenanceJobId;
import com.spottrack.platform.shared.domain.model.aggregates.AbstractDomainAggregateRoot;
import lombok.Getter;

import java.util.UUID;

@Getter
public class MaintenanceJob extends AbstractDomainAggregateRoot<MaintenanceJob> {

    private MaintenanceJobId jobId;
    private String technicianId;
    private String maintenanceId;
    private boolean accepted;

    protected MaintenanceJob() {}

    public MaintenanceJob(String maintenanceId) {
        this.jobId = new MaintenanceJobId(UUID.randomUUID().toString());
        this.maintenanceId = maintenanceId;
        this.accepted = false;
    }

    public MaintenanceJob(String jobId, String maintenanceId, String technicianId, boolean accepted) {
        this.jobId = new MaintenanceJobId(jobId);
        this.maintenanceId = maintenanceId;
        this.technicianId = technicianId;
        this.accepted = accepted;
    }

    public void accept(AcceptMaintenance command) {
        if (this.accepted) {
            throw new IllegalStateException("maintenance.error.job.alreadyAccepted");
        }
        this.technicianId = command.technicianId();
        this.accepted = true;
        registerDomainEvent(new MaintenanceJobAcceptedEvent(this.jobId.uuid(), this.maintenanceId, this.technicianId));
    }
}
