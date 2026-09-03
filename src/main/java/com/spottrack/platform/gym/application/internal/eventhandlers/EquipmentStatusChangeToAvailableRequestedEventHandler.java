package com.spottrack.platform.gym.application.internal.eventhandlers;

import com.spottrack.platform.gym.application.commandServices.EquipmentCommandService;
import com.spottrack.platform.gym.domain.model.commands.UpdateEquipmentStatus;
import com.spottrack.platform.gym.domain.model.valueobjects.EquipmentStatus;
import com.spottrack.platform.reservation.interfaces.events.EquipmentStatusChangeToAvailableRequestedIntegrationEvent;
import com.spottrack.platform.shared.application.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Service("gymEquipmentStatusChangeToAvailableRequestedEventHandler")
@Slf4j
public class EquipmentStatusChangeToAvailableRequestedEventHandler {

    private final EquipmentCommandService equipmentCommandService;

    public EquipmentStatusChangeToAvailableRequestedEventHandler(EquipmentCommandService equipmentCommandService) {
        this.equipmentCommandService = equipmentCommandService;
    }

    @EventListener
    public void on(EquipmentStatusChangeToAvailableRequestedIntegrationEvent event) {
        var result = equipmentCommandService.handle(
                new UpdateEquipmentStatus(event.equipmentId(), EquipmentStatus.valueOf(event.status())));

        if (result instanceof Result.Failure(var error)) {
            log.warn("Failed to mark equipment {} as {}: {}", event.equipmentId(), event.status(), error.message());
        }
    }
}
