package com.spottrack.platform.gym.application.internal.eventhandlers;

import com.spottrack.platform.gym.application.commandServices.EquipmentCommandService;
import com.spottrack.platform.gym.domain.model.commands.UpdateEquipmentStatus;
import com.spottrack.platform.gym.domain.model.valueobjects.EquipmentStatus;
import com.spottrack.platform.reservation.interfaces.events.ExpressReservationInitiatedIntegrationEvent;
import com.spottrack.platform.shared.application.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

/**
 * Handles {@link ExpressReservationInitiatedIntegrationEvent} published by the
 * {@code reservation} bounded context.
 *
 * <p>Marks the held equipment as {@code RESERVED} so it is visibly taken while the
 * client walks to it.</p>
 */
@Service("gymExpressReservationInitiatedEventHandler")
@Slf4j
public class ExpressReservationInitiatedEventHandler {

    private final EquipmentCommandService equipmentCommandService;

    public ExpressReservationInitiatedEventHandler(EquipmentCommandService equipmentCommandService) {
        this.equipmentCommandService = equipmentCommandService;
    }

    @EventListener
    public void on(ExpressReservationInitiatedIntegrationEvent event) {
        var result = equipmentCommandService.handle(
                new UpdateEquipmentStatus(event.equipmentId(), EquipmentStatus.RESERVED));

        if (result instanceof Result.Failure<?, ?>(var error)) {
            log.warn("Failed to mark equipment {} as RESERVED for reservation {}: {}",
                    event.equipmentId(), event.reservationId(), error);
        }
    }
}
