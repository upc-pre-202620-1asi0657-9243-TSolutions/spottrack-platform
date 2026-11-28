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
import com.spottrack.platform.iam.interfaces.rest.resources.UserResource;
import com.spottrack.platform.shared.infrastructure.documentation.openapi.configuration.OpenApiConfiguration;
import com.spottrack.platform.shared.interfaces.rest.resources.ErrorResource;
import com.spottrack.platform.shared.interfaces.rest.transform.ErrorResponseAssembler;
import com.spottrack.platform.shared.interfaces.rest.transform.ResponseEntityAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
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
@Tag(name = "Users", description = "User accounts, technician registration and per-user settings")
public class UsersController {

    private final UserCommandService userCommandService;
    private final UserQueryService userQueryService;

    public UsersController(UserCommandService userCommandService, UserQueryService userQueryService) {
        this.userCommandService = userCommandService;
        this.userQueryService = userQueryService;
    }

    @Operation(
            summary = "Create a user with explicit roles",
            description = "Administrator-only. Same behaviour as POST /api/v1/authentication/sign-up-staff: "
                    + "creates an account with the roles in the payload; ROLE_TECHNICIAN is rejected with 422."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "User created",
                    content = @Content(schema = @Schema(implementation = UserResource.class))),
            @ApiResponse(responseCode = "400", ref = OpenApiConfiguration.RESPONSES_REF + "BadRequest"),
            @ApiResponse(responseCode = "401", ref = OpenApiConfiguration.RESPONSES_REF + "Unauthorized"),
            @ApiResponse(responseCode = "403", ref = OpenApiConfiguration.RESPONSES_REF + "Forbidden"),
            @ApiResponse(responseCode = "409", ref = OpenApiConfiguration.RESPONSES_REF + "Conflict"),
            @ApiResponse(responseCode = "422", ref = OpenApiConfiguration.RESPONSES_REF + "UnprocessableEntity")
    })
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
                    ref = OpenApiConfiguration.RESPONSES_REF + "BadRequest"),
            @ApiResponse(responseCode = "401", ref = OpenApiConfiguration.RESPONSES_REF + "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Caller is not an administrator",
                    ref = OpenApiConfiguration.RESPONSES_REF + "Forbidden"),
            @ApiResponse(responseCode = "404", description = "The calling administrator account no longer exists",
                    content = @Content(schema = @Schema(implementation = ErrorResource.class),
                            examples = @ExampleObject(value = """
                                    {"code": "ADMIN_NOT_FOUND", "message": "Admin not found: admin@spottrack.com"}
                                    """))),
            @ApiResponse(responseCode = "409", description = "USER_CONFLICT: the email is already registered",
                    ref = OpenApiConfiguration.RESPONSES_REF + "Conflict")
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

    @Operation(summary = "List all users", description = "Administrator-only. Returns every account with its roles.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Users found",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = UserResource.class)))),
            @ApiResponse(responseCode = "401", ref = OpenApiConfiguration.RESPONSES_REF + "Unauthorized"),
            @ApiResponse(responseCode = "403", ref = OpenApiConfiguration.RESPONSES_REF + "Forbidden")
    })
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<?>> getAllUsers() {
        var users = userQueryService.handle(new GetAllUsersQuery());
        var resources = users.stream()
                .map(UserResourceFromEntityAssembler::toResourceFromEntity)
                .toList();
        return ResponseEntity.ok(resources);
    }

    @Operation(
            summary = "Change own password",
            description = "Changes the authenticated user's password after checking the current one."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Password changed",
                    content = @Content(schema = @Schema(implementation = UserResource.class))),
            @ApiResponse(responseCode = "400", description = "Invalid payload or current password is incorrect",
                    content = @Content(schema = @Schema(implementation = ErrorResource.class),
                            examples = @ExampleObject(value = """
                                    {"code": "VALIDATION_ERROR", "message": "Validation failed: password", "details": "Current password is incorrect."}
                                    """))),
            @ApiResponse(responseCode = "401", ref = OpenApiConfiguration.RESPONSES_REF + "Unauthorized"),
            @ApiResponse(responseCode = "404", ref = OpenApiConfiguration.RESPONSES_REF + "NotFound")
    })
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

    @Operation(
            summary = "Get a user by id",
            description = "Administrators can read any user; other callers only their own account."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User found",
                    content = @Content(schema = @Schema(implementation = UserResource.class))),
            @ApiResponse(responseCode = "401", ref = OpenApiConfiguration.RESPONSES_REF + "Unauthorized"),
            @ApiResponse(responseCode = "403", ref = OpenApiConfiguration.RESPONSES_REF + "Forbidden"),
            @ApiResponse(responseCode = "404", ref = OpenApiConfiguration.RESPONSES_REF + "NotFoundEmpty")
    })
    @GetMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.id")
    public ResponseEntity<?> getUserById(
            @Parameter(description = "User identifier", example = "42") @PathVariable Long userId) {
        var userOptional = userQueryService.handle(new GetUserByIdQuery(userId));
        if (userOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(UserResourceFromEntityAssembler.toResourceFromEntity(userOptional.get()));
    }

    @Operation(summary = "Get the authenticated user", description = "Returns the account behind the bearer token.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User found",
                    content = @Content(schema = @Schema(implementation = UserResource.class))),
            @ApiResponse(responseCode = "401", ref = OpenApiConfiguration.RESPONSES_REF + "Unauthorized"),
            @ApiResponse(responseCode = "404", ref = OpenApiConfiguration.RESPONSES_REF + "NotFoundEmpty")
    })
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(Authentication authentication) {
        var userOptional = userQueryService.handle(new GetUserByUsernameQuery(authentication.getName()));
        if (userOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(UserResourceFromEntityAssembler.toResourceFromEntity(userOptional.get()));
    }

    @Operation(
            summary = "Update own notification preferences",
            description = "Sets which alert severities are emailed to the authenticated user and to which address."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Preferences updated",
                    content = @Content(schema = @Schema(implementation = UserResource.class))),
            @ApiResponse(responseCode = "400", ref = OpenApiConfiguration.RESPONSES_REF + "BadRequest"),
            @ApiResponse(responseCode = "401", ref = OpenApiConfiguration.RESPONSES_REF + "Unauthorized"),
            @ApiResponse(responseCode = "404", ref = OpenApiConfiguration.RESPONSES_REF + "NotFound")
    })
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
