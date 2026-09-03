package com.spottrack.platform.monitoring.application.internal.eventhandler;

import com.spottrack.platform.gym.interfaces.acl.GymContextFacade;
import com.spottrack.platform.iam.interfaces.acl.IamContextFacade;
import com.spottrack.platform.monitoring.interfaces.events.AnomalyReportSubmittedIntegrationEvent;
import com.spottrack.platform.shared.application.commandservices.AlertCommandService;
import com.spottrack.platform.shared.domain.model.commands.CreateAlertCommand;
import com.spottrack.platform.shared.domain.model.valueobjects.AlertSeverity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

/**
 * Consumes {@link AnomalyReportSubmittedIntegrationEvent} to create an Alert
 * for the admin who owns the affected equipment.
 */
@Service
@Slf4j
public class AnomalyReportSubmittedAlertEventHandler {

    private final GymContextFacade gymContextFacade;
    private final IamContextFacade iamContextFacade;
    private final AlertCommandService alertCommandService;

    public AnomalyReportSubmittedAlertEventHandler(GymContextFacade gymContextFacade,
                                                    IamContextFacade iamContextFacade,
                                                    AlertCommandService alertCommandService) {
        this.gymContextFacade = gymContextFacade;
        this.iamContextFacade = iamContextFacade;
        this.alertCommandService = alertCommandService;
    }

    @EventListener
    public void on(AnomalyReportSubmittedIntegrationEvent event) {
        var gymId = gymContextFacade.resolveGymIdForEquipment(event.equipmentId());
        if (gymId.isEmpty()) {
            log.warn("No gym could be resolved for equipment {}; anomaly alert was not created.", event.equipmentId());
            return;
        }
        var adminUserId = gymContextFacade.fetchAdminUserIdByGymId(gymId.get());
        if (adminUserId == 0L) {
            log.warn("Gym {} has no admin user; anomaly alert for equipment {} was not created.", gymId.get(), event.equipmentId());
            return;
        }
        if (!iamContextFacade.shouldNotify(adminUserId, AlertSeverity.CRITICAL)) {
            log.info("Admin {} has muted CRITICAL alerts; anomaly alert for equipment {} was suppressed.", adminUserId, event.equipmentId());
            return;
        }
        alertCommandService.handle(new CreateAlertCommand(
                adminUserId,
                event.equipmentId(),
                AlertSeverity.CRITICAL,
                "Anomaly reported: " + event.anomalyDescription()
        ));
        log.info("Anomaly alert created for admin {} on equipment {}.", adminUserId, event.equipmentId());
    }
}
