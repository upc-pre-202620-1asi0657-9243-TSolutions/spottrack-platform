package com.spottrack.platform.monitoring.application.internal.eventhandler;

import com.spottrack.platform.gym.domain.model.valueobjects.EquipmentStatus;
import com.spottrack.platform.gym.interfaces.acl.GymContextFacade;
import com.spottrack.platform.monitoring.domain.model.events.UsageSessionEndedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

/**
 * A walk-up session marks its equipment OCCUPIED on the first motion (see
 * SessionTrackerCommandServiceImpl), so it frees the equipment again when it ends.
 * Reservation sessions are skipped: the reservation context releases the
 * equipment itself once the reservation ends.
 */
@Service
public class WalkUpSessionEndedEquipmentReleaseEventHandler {
    private final GymContextFacade gymContextFacade;

    public WalkUpSessionEndedEquipmentReleaseEventHandler(GymContextFacade gymContextFacade) {
        this.gymContextFacade = gymContextFacade;
    }

    @EventListener
    public void on(UsageSessionEndedEvent event) {
        if (event.reservationId() != null) return;
        gymContextFacade.findEquipmentById(event.equipmentId())
                .filter(equipment -> equipment.getStatus() == EquipmentStatus.OCCUPIED)
                .ifPresent(equipment -> gymContextFacade.updateEquipmentStatus(event.equipmentId(), EquipmentStatus.AVAILABLE));
    }
}
