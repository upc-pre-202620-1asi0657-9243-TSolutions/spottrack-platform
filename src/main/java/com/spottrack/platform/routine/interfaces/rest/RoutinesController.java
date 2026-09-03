package com.spottrack.platform.routine.interfaces.rest;

import com.spottrack.platform.iam.interfaces.acl.IamContextFacade;
import com.spottrack.platform.profiles.interfaces.acl.ProfilesContextFacade;
import com.spottrack.platform.shared.interfaces.rest.guards.GymMembershipAccessGuard;
import com.spottrack.platform.routine.application.commandservices.RoutineCommandService;
import com.spottrack.platform.routine.application.queryservices.RoutineQueryService;
import com.spottrack.platform.routine.domain.model.queries.GetAllRoutinesByClientIdQuery;
import com.spottrack.platform.routine.domain.model.queries.GetRoutineByIdQuery;
import com.spottrack.platform.routine.domain.model.valueobjects.ClientId;
import com.spottrack.platform.routine.interfaces.rest.resources.AddExerciseBlockResource;
import com.spottrack.platform.routine.interfaces.rest.resources.CreateRoutineResource;
import com.spottrack.platform.routine.interfaces.rest.resources.ExerciseBlockResource;
import com.spottrack.platform.routine.interfaces.rest.resources.RoutineResource;
import com.spottrack.platform.routine.interfaces.rest.transform.AddExerciseBlockCommandFromResourceAssembler;
import com.spottrack.platform.routine.interfaces.rest.transform.CreateRoutineCommandFromResourceAssembler;
import com.spottrack.platform.routine.interfaces.rest.transform.ExerciseBlockResourceFromEntityAssembler;
import com.spottrack.platform.routine.interfaces.rest.transform.RoutineResourceFromEntityAssembler;
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
@RequestMapping(value = "/api/v1/routines", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Routines", description = "Routine management endpoints")
public class RoutinesController {

    private final RoutineCommandService routineCommandService;
    private final RoutineQueryService routineQueryService;
    private final IamContextFacade iamContextFacade;
    private final ProfilesContextFacade profilesContextFacade;
    private final GymMembershipAccessGuard gymMembershipAccessGuard;

    public RoutinesController(
            RoutineCommandService routineCommandService,
            RoutineQueryService routineQueryService,
            IamContextFacade iamContextFacade,
            ProfilesContextFacade profilesContextFacade,
            GymMembershipAccessGuard gymMembershipAccessGuard) {
        this.routineCommandService = routineCommandService;
        this.routineQueryService = routineQueryService;
        this.iamContextFacade = iamContextFacade;
        this.profilesContextFacade = profilesContextFacade;
        this.gymMembershipAccessGuard = gymMembershipAccessGuard;
    }

    @PostMapping
    @Operation(
            summary = "Create a new routine",
            description = "Creates a new routine for the authenticated client."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Routine created successfully",
                    content = @Content(schema = @Schema(implementation = RoutineResource.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "404", description = "Client profile not found")
    })
    public ResponseEntity<?> createRoutine(
            Authentication authentication,
            @Valid @RequestBody CreateRoutineResource resource) {
        var clientId = resolveClientId(authentication);
        if (clientId == 0L) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(
                    ApplicationError.notFound("Client", authentication.getName()));
        }
        var membershipError = gymMembershipAccessGuard.check(clientId);
        if (membershipError.isPresent()) return membershipError.get();
        var command = CreateRoutineCommandFromResourceAssembler.toCommandFromResource(resource, clientId);
        var result = routineCommandService.handle(command);
        return ResponseEntityAssembler.toResponseEntityFromResult(
                result,
                RoutineResourceFromEntityAssembler::toResourceFromEntity,
                HttpStatus.CREATED
        );
    }

    @GetMapping("/{routineId}")
    @Operation(
            summary = "Get routine by ID",
            description = "Retrieves a routine by its unique identifier. Returns 403 if the routine does not belong to the authenticated client."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Routine found",
                    content = @Content(schema = @Schema(implementation = RoutineResource.class))
            ),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Routine not found")
    })
    public ResponseEntity<?> getRoutineById(
            Authentication authentication,
            @PathVariable
            @Parameter(description = "Routine unique identifier", example = "1", required = true)
            Long routineId
    ) {
        var clientId = resolveClientId(authentication);
        if (clientId == 0L) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(
                    ApplicationError.notFound("Client", authentication.getName()));
        }
        var routine = routineQueryService.handle(new GetRoutineByIdQuery(routineId));
        if (routine.isEmpty()) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(
                    ApplicationError.notFound("Routine", routineId.toString()));
        }
        var ownershipError = checkOwnership(routine.get().getClientId().clientId(), clientId, routineId.toString());
        if (ownershipError.isPresent()) return ownershipError.get();
        return ResponseEntity.ok(RoutineResourceFromEntityAssembler.toResourceFromEntity(routine.get()));
    }

    @GetMapping("/{routineId}/exercise-blocks")
    @Operation(
            summary = "Get exercise blocks for a routine",
            description = "Retrieves the exercise blocks belonging to a routine. Returns 403 if the routine does not belong to the authenticated client."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Exercise blocks retrieved successfully",
                    content = @Content(schema = @Schema(implementation = ExerciseBlockResource.class))
            ),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Routine not found")
    })
    public ResponseEntity<?> getExerciseBlocksByRoutine(
            Authentication authentication,
            @PathVariable
            @Parameter(description = "Routine unique identifier", example = "1", required = true)
            Long routineId
    ) {
        var clientId = resolveClientId(authentication);
        if (clientId == 0L) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(
                    ApplicationError.notFound("Client", authentication.getName()));
        }
        var routine = routineQueryService.handle(new GetRoutineByIdQuery(routineId));
        if (routine.isEmpty()) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(
                    ApplicationError.notFound("Routine", routineId.toString()));
        }
        var ownershipError = checkOwnership(routine.get().getClientId().clientId(), clientId, routineId.toString());
        if (ownershipError.isPresent()) return ownershipError.get();
        var resources = routine.get().getExerciseBlocks().stream()
                .map(ExerciseBlockResourceFromEntityAssembler::toResourceFromEntity)
                .toList();
        return ResponseEntity.ok(resources);
    }

    @GetMapping
    @Operation(
            summary = "Get all routines",
            description = "Retrieves all routines belonging to the authenticated client."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Routines retrieved successfully",
                    content = @Content(schema = @Schema(implementation = RoutineResource.class))
            ),
            @ApiResponse(responseCode = "404", description = "Client profile not found")
    })
    public ResponseEntity<?> getAllRoutines(Authentication authentication) {
        var clientId = resolveClientId(authentication);
        if (clientId == 0L) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(
                    ApplicationError.notFound("Client", authentication.getName()));
        }
        var query = new GetAllRoutinesByClientIdQuery(new ClientId(clientId));
        var resources = routineQueryService.handle(query).stream()
                .map(RoutineResourceFromEntityAssembler::toResourceFromEntity)
                .toList();
        return ResponseEntity.ok(resources);
    }

    @PostMapping("/{routineId}/exercise-blocks")
    @Operation(
            summary = "Add an exercise block to a routine",
            description = "Adds a new exercise block to an existing routine. Returns 403 if the routine does not belong to the authenticated client."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Exercise block added successfully",
                    content = @Content(schema = @Schema(implementation = ExerciseBlockResource.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Routine not found")
    })
    public ResponseEntity<?> addExerciseBlock(
            Authentication authentication,
            @PathVariable
            @Parameter(description = "Routine unique identifier", example = "1", required = true)
            Long routineId,
            @Valid @RequestBody AddExerciseBlockResource resource
    ) {
        var clientId = resolveClientId(authentication);
        if (clientId == 0L) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(
                    ApplicationError.notFound("Client", authentication.getName()));
        }
        var routine = routineQueryService.handle(new GetRoutineByIdQuery(routineId));
        if (routine.isEmpty()) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(
                    ApplicationError.notFound("Routine", routineId.toString()));
        }
        var ownershipError = checkOwnership(routine.get().getClientId().clientId(), clientId, routineId.toString());
        if (ownershipError.isPresent()) return ownershipError.get();
        var resourceWithId = new AddExerciseBlockResource(
                routineId,
                resource.exerciseName(),
                resource.exerciseType(),
                resource.order(),
                resource.sets(),
                resource.reps());
        var command = AddExerciseBlockCommandFromResourceAssembler.toCommandFromResource(resourceWithId);
        var result = routineCommandService.handle(command);
        return ResponseEntityAssembler.toResponseEntityFromResult(
                result,
                ExerciseBlockResourceFromEntityAssembler::toResourceFromEntity,
                HttpStatus.CREATED
        );
    }

    private Long resolveClientId(Authentication authentication) {
        return profilesContextFacade.fetchClientIdByEmail(authentication.getName());
    }

    private Optional<ResponseEntity<?>> checkOwnership(Long routineClientId, Long callerClientId, String routineId) {
        if (!routineClientId.equals(callerClientId)) {
            return Optional.of(ErrorResponseAssembler.toErrorResponseFromApplicationError(
                    ApplicationError.forbidden("Routine", "routineId:" + routineId)));
        }
        return Optional.empty();
    }
}
