package com.spottrack.platform.iam.interfaces.rest;

import com.spottrack.platform.iam.application.commandservices.UserCommandService;
import com.spottrack.platform.iam.domain.model.commands.DeactivateAccountCommand;
import com.spottrack.platform.iam.domain.model.commands.SignOutCommand;
import com.spottrack.platform.iam.domain.model.commands.SignUpCommand;
import com.spottrack.platform.iam.interfaces.rest.resources.ForgotPasswordResource;
import com.spottrack.platform.iam.interfaces.rest.resources.ForgotPasswordVerifyResource;
import com.spottrack.platform.iam.interfaces.rest.resources.PublicSignUpResource;
import com.spottrack.platform.iam.interfaces.rest.resources.SignInResource;
import com.spottrack.platform.iam.interfaces.rest.resources.SignUpResource;
import com.spottrack.platform.iam.interfaces.rest.transform.AuthenticatedUserResourceFromEntityAssembler;
import com.spottrack.platform.iam.interfaces.rest.transform.ForgotPasswordVerifyCommandFromResourceAssembler;
import com.spottrack.platform.iam.interfaces.rest.transform.SignInCommandFromResourceAssembler;
import com.spottrack.platform.iam.interfaces.rest.transform.SignUpCommandFromResourceAssembler;
import com.spottrack.platform.iam.interfaces.rest.transform.UserResourceFromEntityAssembler;
import com.spottrack.platform.iam.interfaces.rest.resources.UserResource;
import com.spottrack.platform.shared.infrastructure.documentation.openapi.configuration.OpenApiConfiguration;
import com.spottrack.platform.shared.interfaces.rest.transform.ResponseEntityAssembler;
import com.spottrack.platform.iam.interfaces.rest.resources.AuthenticatedUserResource;
import com.spottrack.platform.shared.interfaces.rest.resources.ErrorResource;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

import java.util.List;

@RestController
@RequestMapping("/api/v1/authentication")
@Tag(name = "Authentication", description = "Sign-in, sign-up, password recovery and session lifecycle")
public class AuthenticationController {

    private final UserCommandService userCommandService;

    public AuthenticationController(UserCommandService userCommandService) {
        this.userCommandService = userCommandService;
    }

    @Operation(
            summary = "Sign in",
            description = "Authenticates a user with username (email) and password and returns a JWT bearer token "
                    + "together with the user's role, so the client can route to the matching dashboard."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Signed in successfully",
                    content = @Content(schema = @Schema(implementation = AuthenticatedUserResource.class))),
            @ApiResponse(responseCode = "400", ref = OpenApiConfiguration.RESPONSES_REF + "BadRequest"),
            @ApiResponse(responseCode = "401", description = "Invalid credentials",
                    content = @Content(schema = @Schema(implementation = ErrorResource.class),
                            examples = @ExampleObject(value = """
                                    {"code": "UNAUTHORIZED", "message": "Authentication failed", "details": "Invalid credentials"}
                                    """))),
            @ApiResponse(responseCode = "422", description = "The account has been deactivated",
                    content = @Content(schema = @Schema(implementation = ErrorResource.class),
                            examples = @ExampleObject(value = """
                                    {"code": "BUSINESS_RULE_VIOLATION", "message": "Business rule violation: account.deactivated", "details": "This account has been deactivated"}
                                    """)))
    })
    @SecurityRequirements
    @PostMapping("/sign-in")
    public ResponseEntity<?> signIn(@Valid @RequestBody SignInResource resource) {
        var command = SignInCommandFromResourceAssembler.toCommandFromResource(resource);
        var result = userCommandService.handle(command);
        return ResponseEntityAssembler.toResponseEntityFromResult(
                result,
                AuthenticatedUserResourceFromEntityAssembler::toResourceFromEntity,
                HttpStatus.OK
        );
    }

    @Operation(
            summary = "Public client sign-up",
            description = "Creates a client account from a username (email) and password. Public endpoint: "
                    + "no token required. Fails with 409 when the email is already registered or has a "
                    + "pending business registration."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Account created",
                    content = @Content(schema = @Schema(implementation = UserResource.class))),
            @ApiResponse(responseCode = "400", ref = OpenApiConfiguration.RESPONSES_REF + "BadRequest"),
            @ApiResponse(responseCode = "409", ref = OpenApiConfiguration.RESPONSES_REF + "Conflict")
    })
    @SecurityRequirements
    @PostMapping("/sign-up")
    public ResponseEntity<?> signUp(@Valid @RequestBody PublicSignUpResource resource) {
        var command = new SignUpCommand(resource.username(), resource.password(), List.of());
        var result = userCommandService.handle(command);
        return ResponseEntityAssembler.toResponseEntityFromResult(
                result,
                UserResourceFromEntityAssembler::toResourceFromEntity,
                HttpStatus.CREATED
        );
    }

    @Operation(
            summary = "Create a staff account with explicit roles",
            description = "Administrator-only. Creates an account with the roles given in the payload "
                    + "(for example ROLE_ADMIN). Technician accounts must be created through "
                    + "POST /api/v1/users/technicians and are rejected here with 422."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Staff account created",
                    content = @Content(schema = @Schema(implementation = UserResource.class))),
            @ApiResponse(responseCode = "400", ref = OpenApiConfiguration.RESPONSES_REF + "BadRequest"),
            @ApiResponse(responseCode = "403", ref = OpenApiConfiguration.RESPONSES_REF + "Forbidden"),
            @ApiResponse(responseCode = "409", ref = OpenApiConfiguration.RESPONSES_REF + "Conflict"),
            @ApiResponse(responseCode = "422", description = "ROLE_TECHNICIAN requested through this endpoint",
                    content = @Content(schema = @Schema(implementation = ErrorResource.class),
                            examples = @ExampleObject(value = """
                                    {"code": "BUSINESS_RULE_VIOLATION", "message": "Business rule violation: technician-role-restricted", "details": "Technician accounts can only be created by an administrator through POST /api/v1/users/technicians"}
                                    """)))
    })
    @PostMapping("/sign-up-staff")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> signUpStaff(@Valid @RequestBody SignUpResource resource) {
        var command = SignUpCommandFromResourceAssembler.toCommandFromResource(resource);
        var result = userCommandService.handle(command);
        return ResponseEntityAssembler.toResponseEntityFromResult(
                result,
                UserResourceFromEntityAssembler::toResourceFromEntity,
                HttpStatus.CREATED
        );
    }

    // Step 1: validate email format only — no DB query, no timing difference between
    // existing and non-existing emails.
    @Operation(
            summary = "Start password recovery",
            description = "Step 1 of password recovery. Only validates the email format: the response is the same "
                    + "whether or not an account exists, so callers cannot probe for registered emails. "
                    + "Continue with POST /api/v1/authentication/forgot-password/verify."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Generic acknowledgement",
                    content = @Content(examples = @ExampleObject(value = """
                            {"message": "If an account exists for this email, you may proceed to verify your identity."}
                            """))),
            @ApiResponse(responseCode = "400", ref = OpenApiConfiguration.RESPONSES_REF + "BadRequest")
    })
    @SecurityRequirements
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@Valid @RequestBody ForgotPasswordResource resource) {
        return ResponseEntity.ok(Map.of("message",
                "If an account exists for this email, you may proceed to verify your identity."));
    }

    // Step 2: verify DNI ownership and set new password. All failure paths return the
    // same generic message to avoid leaking account existence.
    @Operation(
            summary = "Verify identity and reset password",
            description = "Step 2 of password recovery. Checks that the DNI matches the account and sets the new "
                    + "password. Every failure returns the same generic 400 so account existence is not leaked."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Password reset",
                    content = @Content(schema = @Schema(implementation = UserResource.class))),
            @ApiResponse(responseCode = "400", description = "Invalid payload or verification failed",
                    content = @Content(schema = @Schema(implementation = ErrorResource.class),
                            examples = @ExampleObject(value = """
                                    {"code": "VALIDATION_ERROR", "message": "Validation failed: credentials", "details": "Verification failed. The provided information does not match our records."}
                                    """)))
    })
    @SecurityRequirements
    @PostMapping("/forgot-password/verify")
    public ResponseEntity<?> forgotPasswordVerify(@Valid @RequestBody ForgotPasswordVerifyResource resource) {
        var command = ForgotPasswordVerifyCommandFromResourceAssembler.toCommandFromResource(resource);
        var result = userCommandService.handle(command);
        return ResponseEntityAssembler.toResponseEntityFromResult(
                result,
                UserResourceFromEntityAssembler::toResourceFromEntity,
                HttpStatus.OK
        );
    }

    @Operation(
            summary = "Sign out",
            description = "Signs out the authenticated user. The JWT stays valid until it expires, so clients "
                    + "must also discard it locally."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Signed out",
                    content = @Content(schema = @Schema(implementation = UserResource.class))),
            @ApiResponse(responseCode = "401", ref = OpenApiConfiguration.RESPONSES_REF + "Unauthorized"),
            @ApiResponse(responseCode = "404", ref = OpenApiConfiguration.RESPONSES_REF + "NotFound")
    })
    @PostMapping("/sign-out")
    public ResponseEntity<?> signOut(Authentication authentication) {
        var command = new SignOutCommand(authentication.getName());
        var result = userCommandService.handle(command);
        return ResponseEntityAssembler.toResponseEntityFromResult(
                result,
                UserResourceFromEntityAssembler::toResourceFromEntity,
                HttpStatus.OK
        );
    }

    @Operation(
            summary = "Deactivate own account",
            description = "Deactivates the authenticated user's account. Later sign-in attempts fail with 422."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Account deactivated",
                    content = @Content(schema = @Schema(implementation = UserResource.class))),
            @ApiResponse(responseCode = "401", ref = OpenApiConfiguration.RESPONSES_REF + "Unauthorized"),
            @ApiResponse(responseCode = "404", ref = OpenApiConfiguration.RESPONSES_REF + "NotFound")
    })
    @PostMapping("/deactivate")
    public ResponseEntity<?> deactivate(Authentication authentication) {
        var command = new DeactivateAccountCommand(authentication.getName());
        var result = userCommandService.handle(command);
        return ResponseEntityAssembler.toResponseEntityFromResult(
                result,
                UserResourceFromEntityAssembler::toResourceFromEntity,
                HttpStatus.OK
        );
    }
}
