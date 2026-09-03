package com.spottrack.platform.maintenance.application.internal.eventhandlers;

import com.spottrack.platform.gym.domain.model.valueobjects.EquipmentStatus;
import com.spottrack.platform.gym.interfaces.events.EquipmentStatusUpdatedIntegrationEvent;
import com.spottrack.platform.maintenance.application.commandServices.MaintenanceCommandService;
import com.spottrack.platform.maintenance.domain.model.commands.RecommendEquipmentTransfer;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Decommissioning itself is a gym-context concern (see
 * EquipmentsController#decomissionEquipment) — this only reacts to it once
 * gym reports the status change, by recommending an equipment transfer.
 */
@Component
public class EquipmentDecommissionedEventHandler {

    private final MaintenanceCommandService maintenanceCommandService;

    public EquipmentDecommissionedEventHandler(MaintenanceCommandService maintenanceCommandService) {
        this.maintenanceCommandService = maintenanceCommandService;
    }

    @EventListener
    public void on(EquipmentStatusUpdatedIntegrationEvent event) {
        if (event.status() != EquipmentStatus.DECOMMISSIONED) {
            return;
        }
        maintenanceCommandService.handle(new RecommendEquipmentTransfer(
                event.equipmentId(),
                "Equipment decommissioned — transfer recommended"
        ));
    }
}
