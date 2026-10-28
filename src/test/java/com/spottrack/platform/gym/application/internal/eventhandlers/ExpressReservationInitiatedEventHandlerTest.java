package com.spottrack.platform.gym.application.internal.eventhandlers;

import com.spottrack.platform.gym.application.commandServices.EquipmentCommandService;
import com.spottrack.platform.gym.domain.model.commands.UpdateEquipmentStatus;
import com.spottrack.platform.gym.domain.model.valueobjects.EquipmentStatus;
import com.spottrack.platform.reservation.interfaces.events.ExpressReservationInitiatedIntegrationEvent;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class ExpressReservationInitiatedEventHandlerTest {

    @Test
    void marksTheHeldEquipmentAsReserved() {
        var commandService = mock(EquipmentCommandService.class);
        var handler = new ExpressReservationInitiatedEventHandler(commandService);

        handler.on(new ExpressReservationInitiatedIntegrationEvent("r-1", "eq-1", 5L));

        verify(commandService).handle(new UpdateEquipmentStatus("eq-1", EquipmentStatus.RESERVED));
    }
}
