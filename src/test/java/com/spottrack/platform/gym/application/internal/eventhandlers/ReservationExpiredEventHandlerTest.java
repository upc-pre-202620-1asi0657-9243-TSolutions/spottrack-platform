package com.spottrack.platform.gym.application.internal.eventhandlers;

import com.spottrack.platform.gym.application.commandServices.EquipmentCommandService;
import com.spottrack.platform.gym.application.queryservices.EquipmentQueryService;
import com.spottrack.platform.gym.domain.model.aggregates.Equipment;
import com.spottrack.platform.gym.domain.model.commands.UpdateEquipmentStatus;
import com.spottrack.platform.gym.domain.model.queries.GetEquipmentById;
import com.spottrack.platform.gym.domain.model.valueobjects.EquipmentId;
import com.spottrack.platform.gym.domain.model.valueobjects.EquipmentStatus;
import com.spottrack.platform.reservation.interfaces.events.ReservationExpiredIntegrationEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ReservationExpiredEventHandlerTest {

    private static final ReservationExpiredIntegrationEvent EVENT =
            new ReservationExpiredIntegrationEvent("r-1", "eq-1", 5L);

    private EquipmentCommandService commandService;
    private EquipmentQueryService queryService;
    private Equipment equipment;
    private ReservationExpiredEventHandler handler;

    @BeforeEach
    void setUp() {
        commandService = mock(EquipmentCommandService.class);
        queryService = mock(EquipmentQueryService.class);
        equipment = mock(Equipment.class);
        handler = new ReservationExpiredEventHandler(commandService, queryService);
        when(queryService.handle(new GetEquipmentById(new EquipmentId("eq-1")))).thenReturn(Optional.of(equipment));
    }

    @Test
    void releasesEquipmentThatIsStillReserved() {
        when(equipment.getStatus()).thenReturn(EquipmentStatus.RESERVED);

        handler.on(EVENT);

        verify(commandService).handle(new UpdateEquipmentStatus("eq-1", EquipmentStatus.AVAILABLE));
    }

    @ParameterizedTest
    @EnumSource(value = EquipmentStatus.class, names = {"RESERVED"}, mode = EnumSource.Mode.EXCLUDE)
    void leavesEquipmentInAnyOtherStatusUntouched(EquipmentStatus status) {
        when(equipment.getStatus()).thenReturn(status);

        handler.on(EVENT);

        verify(commandService, never()).handle(any(UpdateEquipmentStatus.class));
    }

    @Test
    void ignoresUnknownEquipment() {
        when(queryService.handle(new GetEquipmentById(new EquipmentId("eq-1")))).thenReturn(Optional.empty());

        handler.on(EVENT);

        verify(commandService, never()).handle(any(UpdateEquipmentStatus.class));
    }
}
