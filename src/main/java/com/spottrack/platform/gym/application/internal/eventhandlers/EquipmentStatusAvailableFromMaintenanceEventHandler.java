package com.spottrack.platform.gym.application.internal.eventhandlers;

import com.spottrack.platform.gym.application.commandServices.EquipmentCommandService;
import com.spottrack.platform.gym.domain.model.commands.UpdateEquipmentStatus;
import com.spottrack.platform.gym.domain.model.valueobjects.EquipmentStatus;
import com.spottrack.platform.maintenance.interfaces.events.EquipmentStatusAvailableIntegrationEvent;
import com.spottrack.platform.shared.application.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EquipmentStatusAvailableFromMaintenanceEventHandler {

    private final EquipmentCommandService equipmentCommandService;

    public EquipmentStatusAvailableFromMaintenanceEventHandler(EquipmentCommandService equipmentCommandService) {
        this.equipmentCommandService = equipmentCommandService;
    }

    @EventListener
    public void on(EquipmentStatusAvailableIntegrationEvent event) {
        var result = equipmentCommandService.handle(
                new UpdateEquipmentStatus(event.equipmentId(), EquipmentStatus.AVAILABLE));

        if (result instanceof Result.Failure(var error)) {
            log.warn("Failed to mark equipment {} as AVAILABLE: {}", event.equipmentId(), error.message());
        }
    }
}
