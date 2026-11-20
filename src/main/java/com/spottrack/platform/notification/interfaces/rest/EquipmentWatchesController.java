package com.spottrack.platform.notification.interfaces.rest;

import com.spottrack.platform.notification.application.commandservices.NotificationCommandService;
import com.spottrack.platform.notification.application.queryservices.NotificationQueryService;
import com.spottrack.platform.notification.domain.model.aggregates.EquipmentWatch;
import com.spottrack.platform.notification.domain.model.commands.UnwatchEquipmentCommand;
import com.spottrack.platform.notification.domain.model.commands.WatchEquipmentCommand;
import com.spottrack.platform.notification.domain.model.queries.GetActiveEquipmentWatchesQuery;
import com.spottrack.platform.notification.interfaces.rest.resources.EquipmentWatchResource;
import com.spottrack.platform.notification.interfaces.rest.resources.WatchEquipmentResource;
import com.spottrack.platform.notification.interfaces.rest.transform.EquipmentWatchResourceFromEntityAssembler;
import com.spottrack.platform.profiles.interfaces.acl.ProfilesContextFacade;
import com.spottrack.platform.shared.application.result.ApplicationError;
import com.spottrack.platform.shared.application.result.Result;
import com.spottrack.platform.shared.interfaces.rest.resources.ErrorResource;
import com.spottrack.platform.shared.interfaces.rest.transform.ErrorResponseAssembler;
import com.spottrack.platform.shared.interfaces.rest.transform.ResponseEntityAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/api/v1/equipment-watches", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Equipment Watches", description = "Notification bell: be notified when a machine in use or under maintenance becomes available")
public class EquipmentWatchesController {

    private final NotificationCommandService notificationCommandService;
    private final NotificationQueryService notificationQueryService;
    private final ProfilesContextFacade profilesContextFacade;

    public EquipmentWatchesController(NotificationCommandService notificationCommandService,
                                      NotificationQueryService notificationQueryService,
                                      ProfilesContextFacade profilesContextFacade) {
        this.notificationCommandService = notificationCommandService;
        this.notificationQueryService = notificationQueryService;
        this.profilesContextFacade = profilesContextFacade;
    }

    @GetMapping
    @PreAuthorize("hasRole('CLIENT')")
    @Operation(summary = "Get my active bells", description = "Machines on which the authenticated client has the notification bell turned on.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Active bells retrieved",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = EquipmentWatchResource.class)))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid authentication token",
                    content = @Content(schema = @Schema(implementation = ErrorResource.class))),
            @ApiResponse(responseCode = "403", description = "Caller is not a client",
                    content = @Content(schema = @Schema(implementation = ErrorResource.class))),
            @ApiResponse(responseCode = "404", description = "Client profile not found",
                    content = @Content(schema = @Schema(implementation = ErrorResource.class)))
    })
    public ResponseEntity<?> getActiveWatches(Authentication authentication) {
        var clientId = resolveClientId(authentication);
        if (clientId == 0L) {
            return clientNotFound(authentication);
        }
        var resources = notificationQueryService.handle(new GetActiveEquipmentWatchesQuery(clientId)).stream()
                .map(EquipmentWatchResourceFromEntityAssembler::toResourceFromEntity)
                .toList();
        return ResponseEntity.ok(resources);
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('CLIENT')")
    @Operation(summary = "Turn on the notification bell of a machine",
            description = "The client is notified when the machine is released or comes back from maintenance. "
                    + "Only machines in use, reserved, under maintenance or out of service of the client's active gym can be watched. "
                    + "Idempotent: turning on a bell that is already on returns the existing one.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Bell turned on",
                    content = @Content(schema = @Schema(implementation = EquipmentWatchResource.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request body",
                    content = @Content(schema = @Schema(implementation = ErrorResource.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid authentication token",
                    content = @Content(schema = @Schema(implementation = ErrorResource.class))),
            @ApiResponse(responseCode = "403", description = "Caller is not a client, or the machine is not in their active gym",
                    content = @Content(schema = @Schema(implementation = ErrorResource.class))),
            @ApiResponse(responseCode = "404", description = "Equipment or client profile not found",
                    content = @Content(schema = @Schema(implementation = ErrorResource.class))),
            @ApiResponse(responseCode = "422", description = "The machine is already available or was decommissioned",
                    content = @Content(schema = @Schema(implementation = ErrorResource.class)))
    })
    public ResponseEntity<?> watchEquipment(Authentication authentication,
                                            @Valid @RequestBody WatchEquipmentResource resource) {
        var clientId = resolveClientId(authentication);
        if (clientId == 0L) {
            return clientNotFound(authentication);
        }
        Result<EquipmentWatch, ApplicationError> result =
                notificationCommandService.handle(new WatchEquipmentCommand(clientId, resource.equipmentId()));
        return ResponseEntityAssembler.toResponseEntityFromResult(
                result, EquipmentWatchResourceFromEntityAssembler::toResourceFromEntity, HttpStatus.CREATED);
    }

    @DeleteMapping("/{equipmentId}")
    @PreAuthorize("hasRole('CLIENT')")
    @Operation(summary = "Turn off the notification bell of a machine")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Bell turned off"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid authentication token",
                    content = @Content(schema = @Schema(implementation = ErrorResource.class))),
            @ApiResponse(responseCode = "403", description = "Caller is not a client",
                    content = @Content(schema = @Schema(implementation = ErrorResource.class))),
            @ApiResponse(responseCode = "404", description = "The client has no active bell on that machine",
                    content = @Content(schema = @Schema(implementation = ErrorResource.class)))
    })
    public ResponseEntity<?> unwatchEquipment(
            Authentication authentication,
            @PathVariable
            @Parameter(description = "Equipment identifier", required = true)
            String equipmentId) {
        var clientId = resolveClientId(authentication);
        if (clientId == 0L) {
            return clientNotFound(authentication);
        }
        var result = notificationCommandService.handle(new UnwatchEquipmentCommand(clientId, equipmentId));
        return switch (result) {
            case Result.Success<EquipmentWatch, ApplicationError> ignored -> ResponseEntity.noContent().build();
            case Result.Failure<EquipmentWatch, ApplicationError> failure ->
                    ErrorResponseAssembler.toErrorResponseFromApplicationError(failure.error());
        };
    }

    private Long resolveClientId(Authentication authentication) {
        var clientId = profilesContextFacade.fetchClientIdByEmail(authentication.getName());
        return clientId == null ? 0L : clientId;
    }

    private static ResponseEntity<?> clientNotFound(Authentication authentication) {
        return ErrorResponseAssembler.toErrorResponseFromApplicationError(
                ApplicationError.notFound("Client", authentication.getName()));
    }
}
