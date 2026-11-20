package com.spottrack.platform.notification.interfaces.rest;

import com.spottrack.platform.notification.application.commandservices.NotificationCommandService;
import com.spottrack.platform.notification.application.queryservices.NotificationQueryService;
import com.spottrack.platform.notification.domain.model.commands.MarkNotificationAsReadCommand;
import com.spottrack.platform.notification.domain.model.queries.GetClientNotificationsQuery;
import com.spottrack.platform.notification.interfaces.rest.resources.ClientNotificationResource;
import com.spottrack.platform.notification.interfaces.rest.transform.ClientNotificationResourceFromEntityAssembler;
import com.spottrack.platform.profiles.interfaces.acl.ProfilesContextFacade;
import com.spottrack.platform.shared.application.result.ApplicationError;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/api/v1/notifications", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Notifications", description = "In-app notifications of the client: machines released or back from maintenance. "
        + "New ones are also pushed in real time over STOMP to /user/queue/notifications.")
public class NotificationsController {

    private final NotificationCommandService notificationCommandService;
    private final NotificationQueryService notificationQueryService;
    private final ProfilesContextFacade profilesContextFacade;

    public NotificationsController(NotificationCommandService notificationCommandService,
                                   NotificationQueryService notificationQueryService,
                                   ProfilesContextFacade profilesContextFacade) {
        this.notificationCommandService = notificationCommandService;
        this.notificationQueryService = notificationQueryService;
        this.profilesContextFacade = profilesContextFacade;
    }

    @GetMapping
    @PreAuthorize("hasRole('CLIENT')")
    @Operation(summary = "Get my notifications", description = "Notifications of the authenticated client, newest first.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Notifications retrieved",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = ClientNotificationResource.class)))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid authentication token",
                    content = @Content(schema = @Schema(implementation = ErrorResource.class))),
            @ApiResponse(responseCode = "403", description = "Caller is not a client",
                    content = @Content(schema = @Schema(implementation = ErrorResource.class))),
            @ApiResponse(responseCode = "404", description = "Client profile not found",
                    content = @Content(schema = @Schema(implementation = ErrorResource.class)))
    })
    public ResponseEntity<?> getNotifications(
            Authentication authentication,
            @RequestParam(defaultValue = "false")
            @Parameter(description = "Only return the notifications not read yet")
            boolean unreadOnly) {
        var clientId = profilesContextFacade.fetchClientIdByEmail(authentication.getName());
        if (clientId == null || clientId == 0L) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(
                    ApplicationError.notFound("Client", authentication.getName()));
        }
        var resources = notificationQueryService.handle(new GetClientNotificationsQuery(clientId, unreadOnly)).stream()
                .map(ClientNotificationResourceFromEntityAssembler::toResourceFromEntity)
                .toList();
        return ResponseEntity.ok(resources);
    }

    @PatchMapping("/{notificationId}/read")
    @PreAuthorize("hasRole('CLIENT')")
    @Operation(summary = "Mark a notification as read",
            description = "Idempotent: marking an already read notification keeps the original read time.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Notification marked as read",
                    content = @Content(schema = @Schema(implementation = ClientNotificationResource.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid authentication token",
                    content = @Content(schema = @Schema(implementation = ErrorResource.class))),
            @ApiResponse(responseCode = "403", description = "Caller is not a client, or the notification belongs to another client",
                    content = @Content(schema = @Schema(implementation = ErrorResource.class))),
            @ApiResponse(responseCode = "404", description = "Notification or client profile not found",
                    content = @Content(schema = @Schema(implementation = ErrorResource.class)))
    })
    public ResponseEntity<?> markAsRead(
            Authentication authentication,
            @PathVariable
            @Parameter(description = "Notification identifier", example = "1", required = true)
            Long notificationId) {
        var clientId = profilesContextFacade.fetchClientIdByEmail(authentication.getName());
        if (clientId == null || clientId == 0L) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(
                    ApplicationError.notFound("Client", authentication.getName()));
        }
        MarkNotificationAsReadCommand command;
        try {
            command = new MarkNotificationAsReadCommand(notificationId, clientId);
        } catch (IllegalArgumentException e) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(
                    ApplicationError.validationError("notificationId", e.getMessage()));
        }
        return ResponseEntityAssembler.toResponseEntityFromResult(
                notificationCommandService.handle(command),
                ClientNotificationResourceFromEntityAssembler::toResourceFromEntity,
                HttpStatus.OK);
    }
}
