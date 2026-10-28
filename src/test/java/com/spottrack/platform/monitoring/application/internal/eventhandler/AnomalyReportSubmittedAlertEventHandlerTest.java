package com.spottrack.platform.monitoring.application.internal.eventhandler;

import com.spottrack.platform.gym.interfaces.acl.GymContextFacade;
import com.spottrack.platform.iam.interfaces.acl.IamContextFacade;
import com.spottrack.platform.monitoring.interfaces.events.AnomalyReportSubmittedIntegrationEvent;
import com.spottrack.platform.shared.application.commandservices.AlertCommandService;
import com.spottrack.platform.shared.domain.model.commands.CreateAlertCommand;
import com.spottrack.platform.shared.domain.model.valueobjects.AlertSeverity;
import com.spottrack.platform.shared.domain.model.valueobjects.AlertSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AnomalyReportSubmittedAlertEventHandlerTest {

    private static final AnomalyReportSubmittedIntegrationEvent EVENT = new AnomalyReportSubmittedIntegrationEvent(
            "res-1", "eq-1", "zone-1", "Loose cable", LocalDate.of(2026, 10, 27));

    private GymContextFacade gymContextFacade;
    private IamContextFacade iamContextFacade;
    private AlertCommandService alertCommandService;
    private AnomalyReportSubmittedAlertEventHandler handler;

    @BeforeEach
    void setUp() {
        gymContextFacade = mock(GymContextFacade.class);
        iamContextFacade = mock(IamContextFacade.class);
        alertCommandService = mock(AlertCommandService.class);
        handler = new AnomalyReportSubmittedAlertEventHandler(gymContextFacade, iamContextFacade, alertCommandService);
        when(gymContextFacade.resolveGymIdForEquipment("eq-1")).thenReturn(Optional.of("gym-1"));
        when(gymContextFacade.fetchAdminUserIdByGymId("gym-1")).thenReturn(42L);
        when(iamContextFacade.shouldNotify(42L, AlertSeverity.CRITICAL)).thenReturn(true);
    }

    @Test
    void createsAnAnomalyCriticalAlertForTheGymAdmin() {
        handler.on(EVENT);

        verify(alertCommandService).handle(new CreateAlertCommand(42L, "eq-1", AlertSeverity.CRITICAL,
                AlertSource.ANOMALY, "Anomaly reported: Loose cable"));
    }

    @Test
    void createsNothingWhenTheEquipmentHasNoGym() {
        when(gymContextFacade.resolveGymIdForEquipment("eq-1")).thenReturn(Optional.empty());

        handler.on(EVENT);

        verify(alertCommandService, never()).handle(any(CreateAlertCommand.class));
    }

    @Test
    void createsNothingWhenTheGymHasNoAdmin() {
        when(gymContextFacade.fetchAdminUserIdByGymId("gym-1")).thenReturn(0L);

        handler.on(EVENT);

        verify(alertCommandService, never()).handle(any(CreateAlertCommand.class));
    }

    @Test
    void createsNothingWhenTheAdminMutedCriticalAlerts() {
        when(iamContextFacade.shouldNotify(42L, AlertSeverity.CRITICAL)).thenReturn(false);

        handler.on(EVENT);

        verify(alertCommandService, never()).handle(any(CreateAlertCommand.class));
    }
}
