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
import com.spottrack.platform.shared.interfaces.rest.transform.ResponseEntityAssembler;
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
@Tag(name = "Authentication")
public class AuthenticationController {

    private final UserCommandService userCommandService;

    public AuthenticationController(UserCommandService userCommandService) {
        this.userCommandService = userCommandService;
    }

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
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@Valid @RequestBody ForgotPasswordResource resource) {
        return ResponseEntity.ok(Map.of("message",
                "If an account exists for this email, you may proceed to verify your identity."));
    }

    // Step 2: verify DNI ownership and set new password. All failure paths return the
    // same generic message to avoid leaking account existence.
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
