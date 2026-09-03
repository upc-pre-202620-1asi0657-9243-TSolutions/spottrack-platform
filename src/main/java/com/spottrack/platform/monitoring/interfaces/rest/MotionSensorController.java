package com.spottrack.platform.monitoring.interfaces.rest;

import com.spottrack.platform.gym.interfaces.acl.GymContextFacade;
import com.spottrack.platform.iam.interfaces.acl.IamContextFacade;
import com.spottrack.platform.monitoring.application.commandServices.MotionSensorCommandService;
import com.spottrack.platform.monitoring.application.commandServices.SessionTrackerCommandService;
import com.spottrack.platform.monitoring.application.queryServices.MotionSensorQueryService;
import com.spottrack.platform.monitoring.domain.model.aggregates.SessionTracker;
import com.spottrack.platform.monitoring.domain.model.queries.GetAllMotionSensorsQuery;
import com.spottrack.platform.monitoring.interfaces.rest.resources.CaptureMotionSensorReadingResource;
import com.spottrack.platform.monitoring.interfaces.rest.resources.MotionSensorResource;
import com.spottrack.platform.monitoring.interfaces.rest.resources.RegisterMotionSensorResource;
import com.spottrack.platform.monitoring.interfaces.rest.resources.SessionTrackerResource;
import com.spottrack.platform.monitoring.interfaces.rest.transform.CaptureMotionSensorReadingCommandFromResource;
import com.spottrack.platform.monitoring.interfaces.rest.transform.MotionSensorResourceFromEntity;
import com.spottrack.platform.monitoring.interfaces.rest.transform.RegisterMotionSensorCommandFromResource;
import com.spottrack.platform.monitoring.interfaces.rest.transform.SessionTrackerResourceFromEntity;
import com.spottrack.platform.profiles.interfaces.acl.ProfilesContextFacade;
import com.spottrack.platform.reservation.interfaces.acl.ReservationContextFacade;
import com.spottrack.platform.shared.interfaces.rest.transform.ResponseEntityAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = "/api/v1/monitoring/motion-sensors", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "MotionSensors", description = "Motion sensor registration endpoints")
public class MotionSensorController {
    private final MotionSensorCommandService motionSensorCommandService;
    private final MotionSensorQueryService motionSensorQueryService;
    private final SessionTrackerCommandService sessionTrackerCommandService;
    private final GymContextFacade gymContextFacade;
    private final ReservationContextFacade reservationContextFacade;
    private final ProfilesContextFacade profilesContextFacade;
    private final IamContextFacade iamContextFacade;

    public MotionSensorController(MotionSensorCommandService motionSensorCommandService, MotionSensorQueryService motionSensorQueryService,
                                   SessionTrackerCommandService sessionTrackerCommandService, GymContextFacade gymContextFacade,
                                   ReservationContextFacade reservationContextFacade, ProfilesContextFacade profilesContextFacade,
                                   IamContextFacade iamContextFacade) {
        this.motionSensorCommandService = motionSensorCommandService;
        this.motionSensorQueryService = motionSensorQueryService;
        this.sessionTrackerCommandService = sessionTrackerCommandService;
        this.gymContextFacade = gymContextFacade;
        this.reservationContextFacade = reservationContextFacade;
        this.profilesContextFacade = profilesContextFacade;
        this.iamContextFacade = iamContextFacade;
    }

    private SessionTrackerResource toEnrichedResource(SessionTracker tracker) {
        var equipmentName = gymContextFacade.findEquipmentById(tracker.getEquipmentId().uuid())
                .map(equipment -> equipment.getEquipmentName())
                .orElse(null);

        Long clientId = null;
        String clientName = null;
        if (tracker.getReservationId() != null) {
            var clientIdOpt = reservationContextFacade.fetchClientIdByReservationId(tracker.getReservationId().uuid());
            if (clientIdOpt.isPresent()) {
                clientId = clientIdOpt.get();
                clientName = profilesContextFacade.fetchClientNameById(clientId);
            }
        }

        return SessionTrackerResourceFromEntity.toResourceFromEntity(tracker, equipmentName, clientId, clientName, null);
    }

    @GetMapping
    public List<MotionSensorResource> getAllMotionSensors() {
        return motionSensorQueryService.handle(new GetAllMotionSensorsQuery()).stream()
                .map(sensor -> MotionSensorResourceFromEntity.toResourceFromEntity(
                        sensor,
                        gymContextFacade.findEquipmentById(sensor.getEquipmentId().uuid()).orElse(null)
                ))
                .toList();
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('ADMIN')")
    public List<MotionSensorResource> getMyMotionSensors(Authentication authentication) {
        var adminUserId = iamContextFacade.fetchUserIdByUsername(authentication.getName()).orElse(0L);
        Set<String> myEquipmentIds = gymContextFacade.findEquipmentsByAdminUserId(adminUserId).stream()
                .map(equipment -> equipment.getId().uuid())
                .collect(Collectors.toSet());

        return motionSensorQueryService.handle(new GetAllMotionSensorsQuery()).stream()
                .filter(sensor -> myEquipmentIds.contains(sensor.getEquipmentId().uuid()))
                .map(sensor -> MotionSensorResourceFromEntity.toResourceFromEntity(
                        sensor,
                        gymContextFacade.findEquipmentById(sensor.getEquipmentId().uuid()).orElse(null)
                ))
                .toList();
    }

    @PostMapping
    @Operation(summary = "Register a motion sensor", description = "Registers a new motion sensor watching a specific equipment.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Motion sensor registered successfully",
                    content = @Content(schema = @Schema(implementation = MotionSensorResource.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "409", description = "Conflict - a motion sensor is already registered for this equipment")
    })
    public ResponseEntity<?> registerMotionSensor(@RequestBody RegisterMotionSensorResource resource) {
        var command = RegisterMotionSensorCommandFromResource.toCommandFromResource(resource);
        var result = motionSensorCommandService.handle(command);
        return ResponseEntityAssembler.toResponseEntityFromResult(
                result,
                sensor -> MotionSensorResourceFromEntity.toResourceFromEntity(
                        sensor,
                        gymContextFacade.findEquipmentById(sensor.getEquipmentId().uuid()).orElse(null)
                ),
                HttpStatus.CREATED
        );
    }

    @PostMapping("/capture-motion")
    @Operation(summary = "Capture a motion sensor reading", description = "Records a motion sensor reading against a session tracker, refreshing its activity clock when movement is detected.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Reading captured successfully",
                    content = @Content(schema = @Schema(implementation = SessionTrackerResource.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "404", description = "Session tracker not found")
    })
    public ResponseEntity<?> captureMotion(@RequestBody CaptureMotionSensorReadingResource resource) {
        var command = CaptureMotionSensorReadingCommandFromResource.toCommandFromResource(resource);
        var result = sessionTrackerCommandService.handle(command);
        return ResponseEntityAssembler.toResponseEntityFromResult(
                result,
                this::toEnrichedResource,
                HttpStatus.OK
        );
    }
}
