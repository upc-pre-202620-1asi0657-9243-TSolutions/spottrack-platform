package com.spottrack.platform.maintenance.application.internal.eventhandlers;

import com.spottrack.platform.gym.interfaces.events.MaintenanceThresholdReachedIntegrationEvent;
import com.spottrack.platform.maintenance.application.commandServices.MaintenanceCommandService;
import com.spottrack.platform.maintenance.domain.model.commands.RequestMaintenance;
import com.spottrack.platform.maintenance.domain.model.valueobjects.EquipmentId;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Consumes gym's [Threshold reached?] Emit Maintenance Request integration event
 * and auto-opens a RequestMaintenance. This only records that the equipment
 * needs maintenance — an admin still has to manually create the
 * TechnicalTicket that actually drives the work.
 */
@Component
public class MaintenanceThresholdReachedIntegrationEventHandler {

    private final MaintenanceCommandService maintenanceCommandService;

    public MaintenanceThresholdReachedIntegrationEventHandler(MaintenanceCommandService maintenanceCommandService) {
        this.maintenanceCommandService = maintenanceCommandService;
    }

    @EventListener
    public void on(MaintenanceThresholdReachedIntegrationEvent event) {
        maintenanceCommandService.handle(new RequestMaintenance(
                new EquipmentId(event.equipmentId()),
                "SYSTEM_THRESHOLD",
                "Automatic maintenance request: usage threshold reached on " + event.threshold()));
    }
}
