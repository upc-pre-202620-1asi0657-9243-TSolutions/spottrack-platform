package com.spottrack.platform.gym.application.internal.eventhandlers;

import com.spottrack.platform.gym.interfaces.acl.GymContextFacade;
import com.spottrack.platform.gym.interfaces.events.MaintenanceThresholdReachedIntegrationEvent;
import com.spottrack.platform.iam.interfaces.acl.IamContextFacade;
import com.spottrack.platform.shared.application.commandservices.AlertCommandService;
import com.spottrack.platform.shared.domain.model.commands.CreateAlertCommand;
import com.spottrack.platform.shared.domain.model.valueobjects.AlertSeverity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

/**
 * Consumes {@link MaintenanceThresholdReachedIntegrationEvent} to create an Alert
 * for the admin who owns the affected equipment.
 */
@Service
@Slf4j
public class MaintenanceThresholdAlertEventHandler {

    private final GymContextFacade gymContextFacade;
    private final IamContextFacade iamContextFacade;
    private final AlertCommandService alertCommandService;

    public MaintenanceThresholdAlertEventHandler(GymContextFacade gymContextFacade,
                                                  IamContextFacade iamContextFacade,
                                                  AlertCommandService alertCommandService) {
        this.gymContextFacade = gymContextFacade;
        this.iamContextFacade = iamContextFacade;
        this.alertCommandService = alertCommandService;
    }

    @EventListener
    public void on(MaintenanceThresholdReachedIntegrationEvent event) {
        var gymId = gymContextFacade.resolveGymIdForEquipment(event.equipmentId());
        if (gymId.isEmpty()) {
            log.warn("No gym could be resolved for equipment {}; maintenance threshold alert was not created.", event.equipmentId());
            return;
        }
        var adminUserId = gymContextFacade.fetchAdminUserIdByGymId(gymId.get());
        if (adminUserId == 0L) {
            log.warn("Gym {} has no admin user; maintenance threshold alert for equipment {} was not created.", gymId.get(), event.equipmentId());
            return;
        }
        if (!iamContextFacade.shouldNotify(adminUserId, AlertSeverity.WARNING)) {
            log.info("Admin {} has muted WARNING alerts; maintenance threshold alert for equipment {} was suppressed.", adminUserId, event.equipmentId());
            return;
        }
        alertCommandService.handle(new CreateAlertCommand(
                adminUserId,
                event.equipmentId(),
                AlertSeverity.WARNING,
                "Equipment maintenance threshold reached: preventive maintenance required."
        ));
        log.info("Maintenance threshold alert created for admin {} on equipment {}.", adminUserId, event.equipmentId());
    }
}
