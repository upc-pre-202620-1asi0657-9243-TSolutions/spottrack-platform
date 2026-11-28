package com.spottrack.platform.monitoring.interfaces.rest;

import com.spottrack.platform.gym.interfaces.acl.EquipmentSnapshot;
import com.spottrack.platform.gym.interfaces.acl.GymContextFacade;
import com.spottrack.platform.iam.interfaces.acl.IamContextFacade;
import com.spottrack.platform.monitoring.application.commandServices.MotionSensorCommandService;
import com.spottrack.platform.monitoring.application.commandServices.SessionTrackerCommandService;
import com.spottrack.platform.monitoring.application.queryservices.MotionSensorQueryService;
import com.spottrack.platform.monitoring.domain.model.aggregates.MotionSensor;
import com.spottrack.platform.monitoring.domain.model.aggregates.SessionTracker;
import com.spottrack.platform.monitoring.domain.model.commands.MotionSensorCaptureCommand;
import com.spottrack.platform.monitoring.domain.model.queries.GetAllMotionSensorsQuery;
import com.spottrack.platform.monitoring.domain.model.valueobjects.EquipmentId;
import com.spottrack.platform.monitoring.domain.model.valueobjects.MotionSensorId;
import com.spottrack.platform.monitoring.interfaces.rest.resources.CaptureMotionSensorReadingResource;
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
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class MotionSensorControllerTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 11, 27, 10, 0);
    private static final EquipmentSnapshot BIKE =
            new EquipmentSnapshot("eq-1", "Bike", "B-20", EquipmentStatus.OCCUPIED, "zone-1");

    private MotionSensorQueryService queryService;
    private SessionTrackerCommandService sessionTrackerCommandService;
    private GymContextFacade gymContextFacade;
    private ReservationContextFacade reservationContextFacade;
    private IamContextFacade iamContextFacade;
    private MotionSensorController controller;

    @BeforeEach
    void setUp() {
        queryService = mock(MotionSensorQueryService.class);
        sessionTrackerCommandService = mock(SessionTrackerCommandService.class);
        gymContextFacade = mock(GymContextFacade.class);
        reservationContextFacade = mock(ReservationContextFacade.class);
        iamContextFacade = mock(IamContextFacade.class);
        controller = new MotionSensorController(mock(MotionSensorCommandService.class), queryService,
                sessionTrackerCommandService, gymContextFacade, reservationContextFacade,
                mock(ProfilesContextFacade.class), iamContextFacade);
        when(gymContextFacade.findEquipmentSnapshotById("eq-1")).thenReturn(Optional.of(BIKE));
        when(gymContextFacade.findEquipmentSnapshotById("eq-2")).thenReturn(Optional.empty());
    }

    @Test
    void getAllMotionSensorsEnrichesEachSensorWithItsEquipment() {
        when(queryService.handle(any(GetAllMotionSensorsQuery.class))).thenReturn(List.of(sensor("eq-1"), sensor("eq-2")));

        var resources = controller.getAllMotionSensors();

        assertEquals(2, resources.size());
        assertEquals("Bike", resources.get(0).equipmentName());
        assertEquals("OCCUPIED", resources.get(0).equipmentStatus());
        assertNull(resources.get(1).equipmentName());
    }

    @Test
    void getMyMotionSensorsKeepsOnlyTheSensorsOfTheAdminsEquipment() {
        var authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("admin@gym.pe");
        when(iamContextFacade.fetchUserIdByUsername("admin@gym.pe")).thenReturn(Optional.of(7L));
        when(gymContextFacade.findEquipmentSnapshotsByAdminUserId(7L)).thenReturn(List.of(BIKE));
        when(queryService.handle(any(GetAllMotionSensorsQuery.class))).thenReturn(List.of(sensor("eq-1"), sensor("eq-2")));

        var resources = controller.getMyMotionSensors(authentication);

        assertEquals(1, resources.size());
        assertEquals("Bike", resources.get(0).equipmentName());
    }

    @Test
    void captureMotionOfAWalkUpSessionReturnsTheEquipmentNameWithoutClient() {
        var tracker = new SessionTracker(1L, "tracker-1", "eq-1", null, LocalTime.MIDNIGHT, LocalTime.MIDNIGHT,
                true, false, null, null, NOW);
        when(sessionTrackerCommandService.handle(any(MotionSensorCaptureCommand.class))).thenReturn(Result.success(tracker));

        var response = controller.captureMotion(new CaptureMotionSensorReadingResource("eq-1", true));

        assertEquals(200, response.getStatusCode().value());
        var body = assertInstanceOf(SessionTrackerResource.class, response.getBody());
        assertEquals("Bike", body.equipmentName());
        assertNull(body.clientId());
        verifyNoInteractions(reservationContextFacade);
    }

    private static MotionSensor sensor(String equipmentId) {
        return new MotionSensor(1L, new MotionSensorId(), new EquipmentId(equipmentId), NOW, true, NOW, NOW, null);
    }
}
