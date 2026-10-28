package com.spottrack.platform.gym.application.internal.eventhandlers;

import com.spottrack.platform.gym.application.commandServices.EquipmentCommandService;
import com.spottrack.platform.gym.application.queryservices.EquipmentQueryService;
import com.spottrack.platform.gym.domain.model.commands.UpdateEquipmentStatus;
import com.spottrack.platform.gym.domain.model.queries.GetEquipmentById;
import com.spottrack.platform.gym.domain.model.valueobjects.EquipmentId;
import com.spottrack.platform.gym.domain.model.valueobjects.EquipmentStatus;
import com.spottrack.platform.reservation.interfaces.events.ReservationExpiredIntegrationEvent;
import com.spottrack.platform.shared.application.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

/**
 * Handles {@link ReservationExpiredIntegrationEvent} published by the
 * {@code reservation} bounded context.
 *
 * <p>Releases the equipment back to {@code AVAILABLE} only if it is still
 * {@code RESERVED}, so a machine that went into maintenance or was occupied in the
 * meantime is never overwritten.</p>
 */
@Service("gymReservationExpiredEventHandler")
@Slf4j
public class ReservationExpiredEventHandler {

    private final EquipmentCommandService equipmentCommandService;
    private final EquipmentQueryService equipmentQueryService;

    public ReservationExpiredEventHandler(EquipmentCommandService equipmentCommandService,
                                          EquipmentQueryService equipmentQueryService) {
        this.equipmentCommandService = equipmentCommandService;
        this.equipmentQueryService = equipmentQueryService;
    }

    @EventListener
    public void on(ReservationExpiredIntegrationEvent event) {
        var equipment = equipmentQueryService.handle(new GetEquipmentById(new EquipmentId(event.equipmentId())));
        if (equipment.isEmpty() || equipment.get().getStatus() != EquipmentStatus.RESERVED) {
            return;
        }
        var result = equipmentCommandService.handle(
                new UpdateEquipmentStatus(event.equipmentId(), EquipmentStatus.AVAILABLE));

        if (result instanceof Result.Failure<?, ?>(var error)) {
            log.warn("Failed to release equipment {} after reservation {} expired: {}",
                    event.equipmentId(), event.reservationId(), error);
        }
    }
}
