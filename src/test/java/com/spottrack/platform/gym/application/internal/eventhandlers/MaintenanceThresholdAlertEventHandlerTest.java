package com.spottrack.platform.gym.application.internal.eventhandlers;

import com.spottrack.platform.gym.interfaces.acl.GymContextFacade;
import com.spottrack.platform.gym.interfaces.events.MaintenanceThresholdReachedIntegrationEvent;
import com.spottrack.platform.iam.interfaces.acl.IamContextFacade;
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

class MaintenanceThresholdAlertEventHandlerTest {

    private static final MaintenanceThresholdReachedIntegrationEvent EVENT =
            new MaintenanceThresholdReachedIntegrationEvent("eq-1", LocalDate.of(2026, 10, 1));

    private GymContextFacade gymContextFacade;
    private IamContextFacade iamContextFacade;
    private AlertCommandService alertCommandService;
    private MaintenanceThresholdAlertEventHandler handler;

    @BeforeEach
    void setUp() {
        gymContextFacade = mock(GymContextFacade.class);
        iamContextFacade = mock(IamContextFacade.class);
        alertCommandService = mock(AlertCommandService.class);
        handler = new MaintenanceThresholdAlertEventHandler(gymContextFacade, iamContextFacade, alertCommandService);
        when(gymContextFacade.resolveGymIdForEquipment("eq-1")).thenReturn(Optional.of("gym-1"));
        when(gymContextFacade.fetchAdminUserIdByGymId("gym-1")).thenReturn(42L);
        when(iamContextFacade.shouldNotify(42L, AlertSeverity.WARNING)).thenReturn(true);
    }

    @Test
    void createsAMaintenanceWarningAlertForTheGymAdmin() {
        handler.on(EVENT);

        verify(alertCommandService).handle(new CreateAlertCommand(42L, "eq-1", AlertSeverity.WARNING,
                AlertSource.MAINTENANCE, "Equipment maintenance threshold reached: preventive maintenance required."));
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
    void createsNothingWhenTheAdminMutedWarnings() {
        when(iamContextFacade.shouldNotify(42L, AlertSeverity.WARNING)).thenReturn(false);

        handler.on(EVENT);

        verify(alertCommandService, never()).handle(any(CreateAlertCommand.class));
    }
}
