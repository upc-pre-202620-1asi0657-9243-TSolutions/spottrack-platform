package com.spottrack.platform.monitoring.application.internal.eventhandler;

import com.spottrack.platform.gym.interfaces.acl.GymContextFacade;
import com.spottrack.platform.iam.interfaces.acl.IamContextFacade;
import com.spottrack.platform.monitoring.interfaces.events.MotionSensorDisconnectedIntegrationEvent;
import com.spottrack.platform.shared.application.commandservices.AlertCommandService;
import com.spottrack.platform.shared.domain.model.commands.CreateAlertCommand;
import com.spottrack.platform.shared.domain.model.valueobjects.AlertSeverity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

/**
 * Consumes {@link MotionSensorDisconnectedIntegrationEvent} to create an Alert
 * for the admin who owns the affected equipment.
 */
@Service
@Slf4j
public class MotionSensorDisconnectedAlertEventHandler {

    private final GymContextFacade gymContextFacade;
    private final IamContextFacade iamContextFacade;
    private final AlertCommandService alertCommandService;

    public MotionSensorDisconnectedAlertEventHandler(GymContextFacade gymContextFacade,
                                                       IamContextFacade iamContextFacade,
                                                       AlertCommandService alertCommandService) {
        this.gymContextFacade = gymContextFacade;
        this.iamContextFacade = iamContextFacade;
        this.alertCommandService = alertCommandService;
    }

    @EventListener
    public void on(MotionSensorDisconnectedIntegrationEvent event) {
        var gymId = gymContextFacade.resolveGymIdForEquipment(event.equipmentId());
        if (gymId.isEmpty()) {
            log.warn("No gym could be resolved for equipment {}; motion sensor disconnection alert was not created.", event.equipmentId());
            return;
        }
        var adminUserId = gymContextFacade.fetchAdminUserIdByGymId(gymId.get());
        if (adminUserId == 0L) {
            log.warn("Gym {} has no admin user; motion sensor disconnection alert for equipment {} was not created.", gymId.get(), event.equipmentId());
            return;
        }
        if (!iamContextFacade.shouldNotify(adminUserId, AlertSeverity.WARNING)) {
            log.info("Admin {} has muted WARNING alerts; motion sensor disconnection alert for equipment {} was suppressed.", adminUserId, event.equipmentId());
            return;
        }
        alertCommandService.handle(new CreateAlertCommand(
                adminUserId,
                event.equipmentId(),
                AlertSeverity.WARNING,
                "IoT motion sensor disconnected from the network."
        ));
        log.info("Motion sensor disconnection alert created for admin {} on equipment {}.", adminUserId, event.equipmentId());
    }
}
