package com.spottrack.platform.monitoring.application.internal.eventhandlers;

import com.spottrack.platform.gym.interfaces.acl.EquipmentSnapshot;
import com.spottrack.platform.gym.interfaces.acl.GymContextFacade;
import com.spottrack.platform.monitoring.domain.model.events.UsageSessionEndedEvent;
import com.spottrack.platform.monitoring.domain.model.valueobjects.SessionTrackerId;
import com.spottrack.platform.shared.domain.model.valueobjects.EquipmentStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class WalkUpSessionEndedEquipmentReleaseEventHandlerTest {

    private GymContextFacade gymContextFacade;
    private WalkUpSessionEndedEquipmentReleaseEventHandler handler;

    @BeforeEach
    void setUp() {
        gymContextFacade = mock(GymContextFacade.class);
        handler = new WalkUpSessionEndedEquipmentReleaseEventHandler(gymContextFacade);
    }

    @Test
    void releasesTheOccupiedEquipmentOfAWalkUpSession() {
        when(gymContextFacade.findEquipmentSnapshotById("eq-1")).thenReturn(Optional.of(snapshot(EquipmentStatus.OCCUPIED)));

        handler.on(new UsageSessionEndedEvent(new SessionTrackerId("tracker-1"), "eq-1", null));

        verify(gymContextFacade).updateEquipmentStatus("eq-1", EquipmentStatus.AVAILABLE);
    }

    @Test
    void leavesEquipmentThatIsNoLongerOccupied() {
        when(gymContextFacade.findEquipmentSnapshotById("eq-1")).thenReturn(Optional.of(snapshot(EquipmentStatus.MAINTENANCE)));

        handler.on(new UsageSessionEndedEvent(new SessionTrackerId("tracker-1"), "eq-1", null));

        verify(gymContextFacade, never()).updateEquipmentStatus(any(), any());
    }

    @Test
    void skipsReservationSessions() {
        handler.on(new UsageSessionEndedEvent(new SessionTrackerId("tracker-1"), "eq-1", "res-1"));

        verifyNoInteractions(gymContextFacade);
    }

    private static EquipmentSnapshot snapshot(EquipmentStatus status) {
        return new EquipmentSnapshot("eq-1", "Bike", "B-1", status, "zone-1");
    }
}
