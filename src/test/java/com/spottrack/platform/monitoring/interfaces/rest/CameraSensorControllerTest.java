package com.spottrack.platform.monitoring.interfaces.rest;

import com.spottrack.platform.gym.interfaces.acl.EquipmentSnapshot;
import com.spottrack.platform.gym.interfaces.acl.GymContextFacade;
import com.spottrack.platform.iam.interfaces.acl.IamContextFacade;
import com.spottrack.platform.monitoring.application.commandServices.CameraSensorCommandService;
import com.spottrack.platform.monitoring.application.commandServices.SessionTrackerCommandService;
import com.spottrack.platform.monitoring.application.queryservices.CameraSensorQueryService;
import com.spottrack.platform.monitoring.domain.model.aggregates.CameraSensor;
import com.spottrack.platform.monitoring.domain.model.aggregates.SessionTracker;
import com.spottrack.platform.monitoring.domain.model.commands.CameraCaptureMotionCommand;
import com.spottrack.platform.monitoring.domain.model.queries.GetAllCameraSensorsQuery;
import com.spottrack.platform.monitoring.domain.model.valueobjects.CameraSensorId;
import com.spottrack.platform.monitoring.domain.model.valueobjects.EquipmentId;
import com.spottrack.platform.monitoring.interfaces.rest.resources.CaptureCameraMotionResource;
import com.spottrack.platform.monitoring.interfaces.rest.resources.SessionTrackerResource;
import com.spottrack.platform.profiles.interfaces.acl.ProfilesContextFacade;
import com.spottrack.platform.reservation.interfaces.acl.ReservationContextFacade;
import com.spottrack.platform.shared.application.result.Result;
import com.spottrack.platform.shared.domain.model.valueobjects.EquipmentStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CameraSensorControllerTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 11, 27, 10, 0);
    private static final EquipmentSnapshot TREADMILL =
            new EquipmentSnapshot("eq-1", "Treadmill", "T-100", EquipmentStatus.AVAILABLE, "zone-1");

    private CameraSensorQueryService queryService;
    private SessionTrackerCommandService sessionTrackerCommandService;
    private GymContextFacade gymContextFacade;
    private ReservationContextFacade reservationContextFacade;
    private ProfilesContextFacade profilesContextFacade;
    private IamContextFacade iamContextFacade;
    private CameraSensorController controller;

    @BeforeEach
    void setUp() {
        queryService = mock(CameraSensorQueryService.class);
        sessionTrackerCommandService = mock(SessionTrackerCommandService.class);
        gymContextFacade = mock(GymContextFacade.class);
        reservationContextFacade = mock(ReservationContextFacade.class);
        profilesContextFacade = mock(ProfilesContextFacade.class);
        iamContextFacade = mock(IamContextFacade.class);
        controller = new CameraSensorController(mock(CameraSensorCommandService.class), queryService,
                sessionTrackerCommandService, gymContextFacade, reservationContextFacade, profilesContextFacade,
                iamContextFacade);
        when(gymContextFacade.findEquipmentSnapshotById("eq-1")).thenReturn(Optional.of(TREADMILL));
        when(gymContextFacade.findEquipmentSnapshotById("eq-2")).thenReturn(Optional.empty());
    }

    @Test
    void getAllCameraSensorsEnrichesEachSensorWithItsEquipment() {
        when(queryService.handle(any(GetAllCameraSensorsQuery.class))).thenReturn(List.of(sensor("eq-1"), sensor("eq-2")));

        var resources = controller.getAllCameraSensors();

        assertEquals(2, resources.size());
        assertEquals("Treadmill", resources.get(0).equipmentName());
        assertEquals("T-100", resources.get(0).equipmentModel());
        assertEquals("AVAILABLE", resources.get(0).equipmentStatus());
        assertNull(resources.get(1).equipmentName(), "equipment no longer in the gym context");
    }

    @Test
    void getMyCameraSensorsKeepsOnlyTheSensorsOfTheAdminsEquipment() {
        var authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("admin@gym.pe");
        when(iamContextFacade.fetchUserIdByUsername("admin@gym.pe")).thenReturn(Optional.of(7L));
        when(gymContextFacade.findEquipmentSnapshotsByAdminUserId(7L)).thenReturn(List.of(TREADMILL));
        when(queryService.handle(any(GetAllCameraSensorsQuery.class))).thenReturn(List.of(sensor("eq-1"), sensor("eq-2")));

        var resources = controller.getMyCameraSensors(authentication);

        assertEquals(1, resources.size());
        assertEquals("Treadmill", resources.get(0).equipmentName());
    }

    @Test
    void captureMotionReturnsTheTrackerWithEquipmentAndClientNames() {
        var tracker = new SessionTracker(1L, "tracker-1", "eq-1", "res-1", LocalTime.MIDNIGHT, LocalTime.MIDNIGHT,
                true, false, null, null, NOW);
        when(sessionTrackerCommandService.handle(any(CameraCaptureMotionCommand.class))).thenReturn(Result.success(tracker));
        when(reservationContextFacade.fetchClientIdByReservationId("res-1")).thenReturn(Optional.of(42L));
        when(profilesContextFacade.fetchClientNameById(42L)).thenReturn("Ana");

        var response = controller.captureMotion(new CaptureCameraMotionResource("eq-1", true));

        assertEquals(200, response.getStatusCode().value());
        var body = assertInstanceOf(SessionTrackerResource.class, response.getBody());
        assertEquals("Treadmill", body.equipmentName());
        assertEquals(42L, body.clientId());
        assertEquals("Ana", body.clientName());
    }

    private static CameraSensor sensor(String equipmentId) {
        return new CameraSensor(1L, new CameraSensorId(), new EquipmentId(equipmentId), NOW);
    }
}
