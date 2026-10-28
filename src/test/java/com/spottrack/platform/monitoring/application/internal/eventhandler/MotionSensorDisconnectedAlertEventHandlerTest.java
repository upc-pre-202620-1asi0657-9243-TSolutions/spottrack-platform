package com.spottrack.platform.monitoring.application.internal.eventhandler;

import com.spottrack.platform.gym.interfaces.acl.GymContextFacade;
import com.spottrack.platform.iam.interfaces.acl.IamContextFacade;
import com.spottrack.platform.monitoring.interfaces.events.MotionSensorDisconnectedIntegrationEvent;
import com.spottrack.platform.shared.application.commandservices.AlertCommandService;
import com.spottrack.platform.shared.domain.model.commands.CreateAlertCommand;
import com.spottrack.platform.shared.domain.model.valueobjects.AlertSeverity;
import com.spottrack.platform.shared.domain.model.valueobjects.AlertSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MotionSensorDisconnectedAlertEventHandlerTest {

    private static final MotionSensorDisconnectedIntegrationEvent EVENT =
            new MotionSensorDisconnectedIntegrationEvent("sensor-1", "eq-1");

    private GymContextFacade gymContextFacade;
    private IamContextFacade iamContextFacade;
    private AlertCommandService alertCommandService;
    private MotionSensorDisconnectedAlertEventHandler handler;

    @BeforeEach
    void setUp() {
        gymContextFacade = mock(GymContextFacade.class);
        iamContextFacade = mock(IamContextFacade.class);
        alertCommandService = mock(AlertCommandService.class);
        handler = new MotionSensorDisconnectedAlertEventHandler(gymContextFacade, iamContextFacade, alertCommandService);
        when(gymContextFacade.resolveGymIdForEquipment("eq-1")).thenReturn(Optional.of("gym-1"));
        when(gymContextFacade.fetchAdminUserIdByGymId("gym-1")).thenReturn(42L);
        when(iamContextFacade.shouldNotify(42L, AlertSeverity.WARNING)).thenReturn(true);
    }

    @Test
    void createsASensorWarningAlertForTheGymAdmin() {
        handler.on(EVENT);

        verify(alertCommandService).handle(new CreateAlertCommand(42L, "eq-1", AlertSeverity.WARNING,
                AlertSource.SENSOR, "IoT motion sensor disconnected from the network."));
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
