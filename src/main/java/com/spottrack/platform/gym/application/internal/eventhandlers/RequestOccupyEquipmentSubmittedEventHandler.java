package com.spottrack.platform.gym.application.internal.eventhandlers;

import com.spottrack.platform.gym.application.commandServices.EquipmentCommandService;
import com.spottrack.platform.gym.domain.model.commands.UpdateEquipmentStatus;
import com.spottrack.platform.gym.domain.model.valueobjects.EquipmentStatus;
import com.spottrack.platform.reservation.interfaces.events.RequestOccupyEquipmentSubmittedIntegrationEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

/**
 * Handles {@link RequestOccupyEquipmentSubmittedIntegrationEvent} published by the
 * {@code reservation} bounded context.
 *
 * <p>Marks the requested equipment as {@code OCCUPIED} so no other client can reserve it.</p>
 */
@Service("gymRequestOccupyEquipmentSubmittedEventHandler")
@Slf4j
public class RequestOccupyEquipmentSubmittedEventHandler {

    private final EquipmentCommandService equipmentCommandService;

    public RequestOccupyEquipmentSubmittedEventHandler(EquipmentCommandService equipmentCommandService) {
        this.equipmentCommandService = equipmentCommandService;
    }

    @EventListener
    public void on(RequestOccupyEquipmentSubmittedIntegrationEvent event) {
        var result = equipmentCommandService.handle(
                new UpdateEquipmentStatus(event.equipmentId(), EquipmentStatus.OCCUPIED));

        if (result instanceof com.spottrack.platform.shared.application.result.Result.Failure(var error)) {
            log.warn("Failed to mark equipment {} as OCCUPIED for request {}: {}",
                    event.equipmentId(), event.requestId(), error.message());
        }
    }
}
