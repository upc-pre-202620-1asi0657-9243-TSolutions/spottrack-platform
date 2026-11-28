package com.spottrack.platform.monitoring.application.internal.commandservices;

import com.spottrack.platform.gym.interfaces.acl.EquipmentSnapshot;
import com.spottrack.platform.gym.interfaces.acl.GymContextFacade;
import com.spottrack.platform.monitoring.domain.model.aggregates.SessionTracker;
import com.spottrack.platform.monitoring.domain.model.commands.CameraCaptureMotionCommand;
import com.spottrack.platform.monitoring.domain.model.commands.MotionSensorCaptureCommand;
import com.spottrack.platform.monitoring.domain.model.valueobjects.EquipmentId;
import com.spottrack.platform.monitoring.domain.repositories.SessionTrackerRepository;
import com.spottrack.platform.shared.domain.model.valueobjects.EquipmentStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SessionTrackerCommandServiceImplCaptureTest {

    private static final EquipmentId EQUIPMENT = new EquipmentId("eq-1");

    private SessionTrackerRepository repository;
    private GymContextFacade gymContextFacade;
    private SessionTrackerCommandServiceImpl service;

    @BeforeEach
    void setUp() {
        repository = mock(SessionTrackerRepository.class);
        gymContextFacade = mock(GymContextFacade.class);
        service = new SessionTrackerCommandServiceImpl(repository, gymContextFacade);
        when(repository.findActiveByEquipmentId(EQUIPMENT)).thenReturn(Optional.empty());
        when(repository.save(any(SessionTracker.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void sensorMotionOnAvailableEquipmentMarksItOccupiedAndStartsAWalkUpSession() {
        when(gymContextFacade.findEquipmentSnapshotById("eq-1")).thenReturn(Optional.of(snapshot(EquipmentStatus.AVAILABLE)));

        var result = service.handle(new MotionSensorCaptureCommand(EQUIPMENT, true));

        assertTrue(result.isSuccess());
        verify(gymContextFacade).updateEquipmentStatus("eq-1", EquipmentStatus.OCCUPIED);
    }

    @Test
    void cameraMotionOnReservedEquipmentLeavesItsStatusAlone() {
        when(gymContextFacade.findEquipmentSnapshotById("eq-1")).thenReturn(Optional.of(snapshot(EquipmentStatus.RESERVED)));

        var result = service.handle(new CameraCaptureMotionCommand(EQUIPMENT, true));

        assertTrue(result.isSuccess());
        verify(gymContextFacade, never()).updateEquipmentStatus(any(), any());
    }

    @Test
    void cameraMotionOnUnknownEquipmentIsNotFound() {
        when(gymContextFacade.findEquipmentSnapshotById("eq-1")).thenReturn(Optional.empty());

        assertTrue(service.handle(new CameraCaptureMotionCommand(EQUIPMENT, true)).isFailure());
        verify(repository, never()).save(any());
    }

    private static EquipmentSnapshot snapshot(EquipmentStatus status) {
        return new EquipmentSnapshot("eq-1", "Bike", "B-1", status, "zone-1");
    }
}
