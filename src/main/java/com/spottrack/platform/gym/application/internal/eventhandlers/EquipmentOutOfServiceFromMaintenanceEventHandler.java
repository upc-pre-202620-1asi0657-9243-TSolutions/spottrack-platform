package com.spottrack.platform.gym.application.internal.eventhandlers;

import com.spottrack.platform.gym.application.commandServices.EquipmentCommandService;
import com.spottrack.platform.gym.domain.model.commands.UpdateEquipmentStatus;
import com.spottrack.platform.gym.domain.model.valueobjects.EquipmentStatus;
import com.spottrack.platform.maintenance.interfaces.events.EquipmentOutOfServiceIntegrationEvent;
import com.spottrack.platform.shared.application.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EquipmentOutOfServiceFromMaintenanceEventHandler {

    private final EquipmentCommandService equipmentCommandService;

    public EquipmentOutOfServiceFromMaintenanceEventHandler(EquipmentCommandService equipmentCommandService) {
        this.equipmentCommandService = equipmentCommandService;
    }

    @EventListener
    public void on(EquipmentOutOfServiceIntegrationEvent event) {
        var result = equipmentCommandService.handle(
                new UpdateEquipmentStatus(event.equipmentId(), EquipmentStatus.OUT_OF_SERVICE));

        if (result instanceof Result.Failure(var error)) {
            log.warn("Failed to mark equipment {} as OUT_OF_SERVICE: {}", event.equipmentId(), error.message());
        }
    }
}
