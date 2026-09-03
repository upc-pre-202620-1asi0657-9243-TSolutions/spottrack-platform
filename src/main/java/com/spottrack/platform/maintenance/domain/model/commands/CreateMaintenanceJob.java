package com.spottrack.platform.maintenance.domain.model.commands;

/**
 * Spawns an unassigned MaintenanceJob for a Maintenance whose TechnicalTicket
 * was just opened, so a Technician has something to accept (see
 * TechnicalTicketCreatedEventHandler).
 */
public record CreateMaintenanceJob(String maintenanceId) {
    public CreateMaintenanceJob {
        if (maintenanceId == null || maintenanceId.isBlank())
            throw new IllegalArgumentException("maintenance.command.createMaintenanceJob.maintenanceId.notBlank");
    }
}
