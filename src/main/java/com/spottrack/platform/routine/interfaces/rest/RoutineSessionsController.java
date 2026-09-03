package com.spottrack.platform.routine.interfaces.rest;

import com.spottrack.platform.iam.interfaces.acl.IamContextFacade;
import com.spottrack.platform.profiles.interfaces.acl.ProfilesContextFacade;
import com.spottrack.platform.shared.interfaces.rest.guards.GymMembershipAccessGuard;
import com.spottrack.platform.routine.application.commandservices.RoutineSessionCommandService;
import com.spottrack.platform.routine.application.queryservices.RoutineSessionQueryService;
import com.spottrack.platform.routine.domain.model.commands.CompleteRoutineCommand;
import com.spottrack.platform.routine.domain.model.commands.MarkRoutineMissedCommand;
import com.spottrack.platform.routine.domain.model.commands.SetExerciseBlockCompletionCommand;
import com.spottrack.platform.routine.domain.model.queries.GetAllRoutineSessionsByClientIdQuery;
import com.spottrack.platform.routine.domain.model.queries.GetRoutineSessionByIdQuery;
import com.spottrack.platform.routine.domain.model.valueobjects.ClientId;
import com.spottrack.platform.routine.interfaces.rest.resources.RoutineSessionResource;
import com.spottrack.platform.routine.interfaces.rest.resources.SetExerciseBlockCompletionResource;
import com.spottrack.platform.routine.interfaces.rest.resources.StartRoutineResource;
import com.spottrack.platform.routine.interfaces.rest.transform.RoutineSessionResourceFromEntityAssembler;
import com.spottrack.platform.routine.interfaces.rest.transform.StartRoutineCommandFromResourceAssembler;
import com.spottrack.platform.shared.application.result.ApplicationError;
import com.spottrack.platform.shared.interfaces.rest.transform.ErrorResponseAssembler;
import com.spottrack.platform.shared.interfaces.rest.transform.ResponseEntityAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping(value = "/api/v1/routine-sessions", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "RoutineSessions", description = "Routine session management endpoints")
public class RoutineSessionsController {

    private final RoutineSessionCommandService routineSessionCommandService;
    private final RoutineSessionQueryService routineSessionQueryService;
    private final IamContextFacade iamContextFacade;
    private final ProfilesContextFacade profilesContextFacade;
    private final GymMembershipAccessGuard gymMembershipAccessGuard;

    public RoutineSessionsController(
            RoutineSessionCommandService routineSessionCommandService,
            RoutineSessionQueryService routineSessionQueryService,
            IamContextFacade iamContextFacade,
            ProfilesContextFacade profilesContextFacade,
            GymMembershipAccessGuard gymMembershipAccessGuard) {
        this.routineSessionCommandService = routineSessionCommandService;
        this.routineSessionQueryService = routineSessionQueryService;
        this.iamContextFacade = iamContextFacade;
        this.profilesContextFacade = profilesContextFacade;
        this.gymMembershipAccessGuard = gymMembershipAccessGuard;
    }

    @PostMapping
    @Operation(summary = "Start a routine session", description = "Starts a new routine session for the authenticated client.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Routine session started successfully",
                    content = @Content(schema = @Schema(implementation = RoutineSessionResource.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "404", description = "Client profile not found")
    })
    public ResponseEntity<?> startRoutineSession(
            Authentication authentication,
            @Valid @RequestBody StartRoutineResource resource) {
        var clientId = resolveClientId(authentication);
        if (clientId == 0L) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(
                    ApplicationError.notFound("Client", authentication.getName()));
        }
        var membershipError = gymMembershipAccessGuard.check(clientId);
        if (membershipError.isPresent()) return membershipError.get();
        var command = StartRoutineCommandFromResourceAssembler.toCommandFromResource(resource, clientId);
        var result = routineSessionCommandService.handle(command);
        return ResponseEntityAssembler.toResponseEntityFromResult(
                result,
                RoutineSessionResourceFromEntityAssembler::toResourceFromEntity,
                HttpStatus.CREATED
        );
    }

    @GetMapping("/{routineSessionId}")
    @Operation(summary = "Get routine session by ID", description = "Retrieves a routine session by its unique identifier. Returns 403 if the session does not belong to the authenticated client.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Routine session found",
                    content = @Content(schema = @Schema(implementation = RoutineSessionResource.class))),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Routine session not found")
    })
    public ResponseEntity<?> getRoutineSessionById(
            Authentication authentication,
            @PathVariable
            @Parameter(description = "Routine session unique identifier", example = "1", required = true)
            Long routineSessionId
    ) {
        var clientId = resolveClientId(authentication);
        if (clientId == 0L) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(
                    ApplicationError.notFound("Client", authentication.getName()));
        }
        var session = routineSessionQueryService.handle(new GetRoutineSessionByIdQuery(routineSessionId));
        if (session.isEmpty()) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(
                    ApplicationError.notFound("RoutineSession", routineSessionId.toString()));
        }
        var ownershipError = checkOwnership(session.get().getClientId().clientId(), clientId, routineSessionId.toString());
        if (ownershipError.isPresent()) return ownershipError.get();
        return ResponseEntity.ok(RoutineSessionResourceFromEntityAssembler.toResourceFromEntity(session.get()));
    }

    @GetMapping
    @Operation(summary = "Get all routine sessions", description = "Retrieves all routine sessions belonging to the authenticated client.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Routine sessions retrieved successfully",
                    content = @Content(schema = @Schema(implementation = RoutineSessionResource.class))),
            @ApiResponse(responseCode = "404", description = "Client profile not found")
    })
    public ResponseEntity<?> getAllRoutineSessions(Authentication authentication) {
        var clientId = resolveClientId(authentication);
        if (clientId == 0L) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(
                    ApplicationError.notFound("Client", authentication.getName()));
        }
        var query = new GetAllRoutineSessionsByClientIdQuery(new ClientId(clientId));
        var resources = routineSessionQueryService.handle(query).stream()
                .map(RoutineSessionResourceFromEntityAssembler::toResourceFromEntity)
                .toList();
        return ResponseEntity.ok(resources);
    }

    @PostMapping("/{routineSessionId}/completions")
    @Operation(summary = "Complete a routine session", description = "Marks a routine session as completed. Returns 403 if the session does not belong to the authenticated client.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Routine session completed successfully",
                    content = @Content(schema = @Schema(implementation = RoutineSessionResource.class))),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Routine session not found")
    })
    public ResponseEntity<?> completeRoutineSession(
            Authentication authentication,
            @PathVariable
            @Parameter(description = "Routine session unique identifier", example = "1", required = true)
            Long routineSessionId
    ) {
        var clientId = resolveClientId(authentication);
        if (clientId == 0L) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(
                    ApplicationError.notFound("Client", authentication.getName()));
        }
        var membershipError = gymMembershipAccessGuard.check(clientId);
        if (membershipError.isPresent()) return membershipError.get();
        var session = routineSessionQueryService.handle(new GetRoutineSessionByIdQuery(routineSessionId));
        if (session.isEmpty()) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(
                    ApplicationError.notFound("RoutineSession", routineSessionId.toString()));
        }
        var ownershipError = checkOwnership(session.get().getClientId().clientId(), clientId, routineSessionId.toString());
        if (ownershipError.isPresent()) return ownershipError.get();
        var command = new CompleteRoutineCommand(routineSessionId);
        var result = routineSessionCommandService.handle(command);
        return ResponseEntityAssembler.toResponseEntityFromResult(
                result,
                RoutineSessionResourceFromEntityAssembler::toResourceFromEntity,
                HttpStatus.OK
        );
    }

    @PostMapping("/{routineSessionId}/missed")
    @Operation(summary = "Mark routine session as missed", description = "Marks a routine session as missed. Returns 403 if the session does not belong to the authenticated client.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Routine session marked as missed",
                    content = @Content(schema = @Schema(implementation = RoutineSessionResource.class))),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Routine session not found")
    })
    public ResponseEntity<?> markRoutineSessionMissed(
            Authentication authentication,
            @PathVariable
            @Parameter(description = "Routine session unique identifier", example = "1", required = true)
            Long routineSessionId
    ) {
        var clientId = resolveClientId(authentication);
        if (clientId == 0L) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(
                    ApplicationError.notFound("Client", authentication.getName()));
        }
        var session = routineSessionQueryService.handle(new GetRoutineSessionByIdQuery(routineSessionId));
        if (session.isEmpty()) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(
                    ApplicationError.notFound("RoutineSession", routineSessionId.toString()));
        }
        var ownershipError = checkOwnership(session.get().getClientId().clientId(), clientId, routineSessionId.toString());
        if (ownershipError.isPresent()) return ownershipError.get();
        var command = new MarkRoutineMissedCommand(routineSessionId);
        var result = routineSessionCommandService.handle(command);
        return ResponseEntityAssembler.toResponseEntityFromResult(
                result,
                RoutineSessionResourceFromEntityAssembler::toResourceFromEntity,
                HttpStatus.OK
        );
    }

    @PatchMapping("/{routineSessionId}/exercise-blocks/{exerciseBlockId}")
    @Operation(summary = "Mark an exercise block as completed or not", description = "Sets the completion state of an exercise block for this routine session. Returns 403 if the session does not belong to the authenticated client.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Completion state updated successfully",
                    content = @Content(schema = @Schema(implementation = RoutineSessionResource.class))),
            @ApiResponse(responseCode = "400", description = "Exercise block does not belong to this routine"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Routine session not found")
    })
    public ResponseEntity<?> setExerciseBlockCompletion(
            Authentication authentication,
            @PathVariable
            @Parameter(description = "Routine session unique identifier", example = "1", required = true)
            Long routineSessionId,
            @PathVariable
            @Parameter(description = "Exercise block unique identifier", example = "1", required = true)
            Long exerciseBlockId,
            @Valid @RequestBody SetExerciseBlockCompletionResource resource
    ) {
        var clientId = resolveClientId(authentication);
        if (clientId == 0L) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(
                    ApplicationError.notFound("Client", authentication.getName()));
        }
        var session = routineSessionQueryService.handle(new GetRoutineSessionByIdQuery(routineSessionId));
        if (session.isEmpty()) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(
                    ApplicationError.notFound("RoutineSession", routineSessionId.toString()));
        }
        var ownershipError = checkOwnership(session.get().getClientId().clientId(), clientId, routineSessionId.toString());
        if (ownershipError.isPresent()) return ownershipError.get();
        var command = new SetExerciseBlockCompletionCommand(routineSessionId, exerciseBlockId, resource.completed());
        var result = routineSessionCommandService.handle(command);
        return ResponseEntityAssembler.toResponseEntityFromResult(
                result,
                RoutineSessionResourceFromEntityAssembler::toResourceFromEntity,
                HttpStatus.OK
        );
    }

    private Long resolveClientId(Authentication authentication) {
        return profilesContextFacade.fetchClientIdByEmail(authentication.getName());
    }

    private Optional<ResponseEntity<?>> checkOwnership(Long sessionClientId, Long callerClientId, String sessionId) {
        if (!sessionClientId.equals(callerClientId)) {
            return Optional.of(ErrorResponseAssembler.toErrorResponseFromApplicationError(
                    ApplicationError.forbidden("RoutineSession", "sessionId:" + sessionId)));
        }
        return Optional.empty();
    }
}
