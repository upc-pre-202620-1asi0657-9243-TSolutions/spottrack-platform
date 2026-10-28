package com.spottrack.platform.iam.interfaces.rest;

import com.spottrack.platform.iam.application.commandservices.UserCommandService;
import com.spottrack.platform.iam.application.queryservices.UserQueryService;
import com.spottrack.platform.iam.domain.model.queries.GetAllUsersQuery;
import com.spottrack.platform.iam.domain.model.queries.GetUserByIdQuery;
import com.spottrack.platform.iam.domain.model.queries.GetUserByUsernameQuery;
import com.spottrack.platform.iam.interfaces.rest.resources.ChangePasswordResource;
import com.spottrack.platform.iam.interfaces.rest.resources.NotificationPreferencesResource;
import com.spottrack.platform.iam.interfaces.rest.resources.RegisterTechnicianResource;
import com.spottrack.platform.iam.interfaces.rest.resources.SignUpResource;
import com.spottrack.platform.iam.interfaces.rest.resources.TechnicianAccountResource;
import com.spottrack.platform.iam.interfaces.rest.transform.ChangePasswordCommandFromResourceAssembler;
import com.spottrack.platform.iam.interfaces.rest.transform.RegisterTechnicianCommandFromResourceAssembler;
import com.spottrack.platform.iam.interfaces.rest.transform.SignUpCommandFromResourceAssembler;
import com.spottrack.platform.iam.interfaces.rest.transform.TechnicianAccountResourceAssembler;
import com.spottrack.platform.iam.interfaces.rest.transform.UpdateNotificationPreferencesCommandFromResourceAssembler;
import com.spottrack.platform.iam.interfaces.rest.transform.UserResourceFromEntityAssembler;
import com.spottrack.platform.shared.application.result.ApplicationError;
import com.spottrack.platform.shared.interfaces.rest.resources.ErrorResource;
import com.spottrack.platform.shared.interfaces.rest.transform.ErrorResponseAssembler;
import com.spottrack.platform.shared.interfaces.rest.transform.ResponseEntityAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "Users")
public class UsersController {

    private final UserCommandService userCommandService;
    private final UserQueryService userQueryService;

    public UsersController(UserCommandService userCommandService, UserQueryService userQueryService) {
        this.userCommandService = userCommandService;
        this.userQueryService = userQueryService;
    }

    @PostMapping("/sign-up")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> signUp(@Valid @RequestBody SignUpResource resource) {
        var command = SignUpCommandFromResourceAssembler.toCommandFromResource(resource);
        var result = userCommandService.handle(command);
        return ResponseEntityAssembler.toResponseEntityFromResult(
                result,
                UserResourceFromEntityAssembler::toResourceFromEntity,
                HttpStatus.CREATED
        );
    }

    @PostMapping("/technicians")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Register a technical staff account",
            description = "Creates a login account with role ROLE_TECHNICIAN and registers it in the maintenance "
                    + "roster of the calling administrator. Technician accounts cannot be created through public sign-up.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Technician account created",
                    content = @Content(schema = @Schema(implementation = TechnicianAccountResource.class))),
            @ApiResponse(responseCode = "400", description = "Invalid payload (email format, password length, blank names)",
                    content = @Content(schema = @Schema(implementation = ErrorResource.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid token",
                    content = @Content(schema = @Schema(implementation = ErrorResource.class))),
            @ApiResponse(responseCode = "403", description = "Caller is not an administrator",
                    content = @Content(schema = @Schema(implementation = ErrorResource.class))),
            @ApiResponse(responseCode = "409", description = "USER_CONFLICT: the email is already registered",
                    content = @Content(schema = @Schema(implementation = ErrorResource.class)))
    })
    public ResponseEntity<?> registerTechnician(@Valid @RequestBody RegisterTechnicianResource resource,
                                                Authentication authentication) {
        var admin = userQueryService.handle(new GetUserByUsernameQuery(authentication.getName()));
        if (admin.isEmpty()) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(
                    ApplicationError.notFound("Admin", authentication.getName()));
        }
        var command = RegisterTechnicianCommandFromResourceAssembler.toCommandFromResource(resource, admin.get().getId());
        var result = userCommandService.handle(command);
        return ResponseEntityAssembler.toResponseEntityFromResult(
                result,
                TechnicianAccountResourceAssembler::toResource,
                HttpStatus.CREATED
        );
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<?>> getAllUsers() {
        var users = userQueryService.handle(new GetAllUsersQuery());
        var resources = users.stream()
                .map(UserResourceFromEntityAssembler::toResourceFromEntity)
                .toList();
        return ResponseEntity.ok(resources);
    }

    @PatchMapping("/me/password")
    public ResponseEntity<?> changePassword(
            @Valid @RequestBody ChangePasswordResource resource,
            Authentication authentication) {
        var command = ChangePasswordCommandFromResourceAssembler.toCommandFromResource(resource, authentication.getName());
        var result = userCommandService.handle(command);
        return ResponseEntityAssembler.toResponseEntityFromResult(
                result,
                UserResourceFromEntityAssembler::toResourceFromEntity,
                HttpStatus.OK
        );
    }

    @GetMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.id")
    public ResponseEntity<?> getUserById(@PathVariable Long userId) {
        var userOptional = userQueryService.handle(new GetUserByIdQuery(userId));
        if (userOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(UserResourceFromEntityAssembler.toResourceFromEntity(userOptional.get()));
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(Authentication authentication) {
        var userOptional = userQueryService.handle(new GetUserByUsernameQuery(authentication.getName()));
        if (userOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(UserResourceFromEntityAssembler.toResourceFromEntity(userOptional.get()));
    }

    @PatchMapping("/me/notification-preferences")
    public ResponseEntity<?> updateNotificationPreferences(
            @Valid @RequestBody NotificationPreferencesResource resource,
            Authentication authentication) {
        var command = UpdateNotificationPreferencesCommandFromResourceAssembler.toCommandFromResource(resource, authentication.getName());
        var result = userCommandService.handle(command);
        return ResponseEntityAssembler.toResponseEntityFromResult(
                result,
                UserResourceFromEntityAssembler::toResourceFromEntity,
                HttpStatus.OK
        );
    }
}
