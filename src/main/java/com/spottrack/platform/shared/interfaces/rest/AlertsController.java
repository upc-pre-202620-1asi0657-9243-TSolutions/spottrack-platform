package com.spottrack.platform.shared.interfaces.rest;

import com.spottrack.platform.iam.interfaces.acl.IamContextFacade;
import com.spottrack.platform.shared.application.commandservices.AlertCommandService;
import com.spottrack.platform.shared.application.queryservices.AlertQueryService;
import com.spottrack.platform.shared.application.result.ApplicationError;
import com.spottrack.platform.shared.application.result.Result;
import com.spottrack.platform.shared.domain.model.aggregates.Alert;
import com.spottrack.platform.shared.domain.model.commands.ClearAlertCommand;
import com.spottrack.platform.shared.domain.model.commands.ClearResolvedAlertsCommand;
import com.spottrack.platform.shared.domain.model.commands.ResolveAlertCommand;
import com.spottrack.platform.shared.domain.model.queries.GetAlertsByAdminUserIdQuery;
import com.spottrack.platform.shared.domain.model.valueobjects.AlertSeverity;
import com.spottrack.platform.shared.domain.model.valueobjects.AlertSource;
import com.spottrack.platform.shared.domain.model.valueobjects.AlertStatusFilter;
import com.spottrack.platform.shared.interfaces.rest.resources.AlertResource;
import com.spottrack.platform.shared.interfaces.rest.transform.AlertResourceFromEntityAssembler;
import com.spottrack.platform.shared.interfaces.rest.transform.ErrorResponseAssembler;
import com.spottrack.platform.shared.interfaces.rest.transform.ResponseEntityAssembler;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.Locale;

@RestController
@RequestMapping(value = "/api/v1/alerts", produces = MediaType.APPLICATION_JSON_VALUE)
public class AlertsController {

    private final AlertCommandService alertCommandService;
    private final AlertQueryService alertQueryService;
    private final IamContextFacade iamContextFacade;

    public AlertsController(AlertCommandService alertCommandService,
                             AlertQueryService alertQueryService,
                             IamContextFacade iamContextFacade) {
        this.alertCommandService = alertCommandService;
        this.alertQueryService = alertQueryService;
        this.iamContextFacade = iamContextFacade;
    }

    @GetMapping
    public ResponseEntity<?> getAlerts(
            Authentication authentication,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String source,
            @RequestParam(required = false) String severity) {
        var adminUserId = resolveAdminUserId(authentication);
        if (adminUserId == 0L) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(
                    ApplicationError.notFound("Admin", authentication.getName()));
        }
        GetAlertsByAdminUserIdQuery query;
        try {
            query = new GetAlertsByAdminUserIdQuery(adminUserId,
                    parseEnum(AlertStatusFilter.class, "status", status),
                    parseEnum(AlertSource.class, "source", source),
                    parseEnum(AlertSeverity.class, "severity", severity));
        } catch (IllegalArgumentException e) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(
                    ApplicationError.validationError("alert filters", e.getMessage()));
        }
        var resources = alertQueryService.handle(query).stream()
                .map(AlertResourceFromEntityAssembler::toResourceFromEntity)
                .toList();
        return ResponseEntity.ok(resources);
    }

    @PatchMapping("/{alertId}/resolve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> resolveAlert(
            Authentication authentication,
            @PathVariable Long alertId) {
        var adminUserId = resolveAdminUserId(authentication);
        if (adminUserId == 0L) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(
                    ApplicationError.notFound("Admin", authentication.getName()));
        }
        var command = new ResolveAlertCommand(alertId, adminUserId);
        var result = alertCommandService.handle(command);
        return ResponseEntityAssembler.toResponseEntityFromResult(
                result,
                AlertResourceFromEntityAssembler::toResourceFromEntity,
                HttpStatus.OK
        );
    }

    @DeleteMapping("/resolved")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> clearResolvedAlerts(Authentication authentication) {
        var adminUserId = resolveAdminUserId(authentication);
        if (adminUserId == 0L) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(
                    ApplicationError.notFound("Admin", authentication.getName()));
        }
        var result = alertCommandService.handle(new ClearResolvedAlertsCommand(adminUserId));
        return switch (result) {
            case Result.Success<Integer, ApplicationError> ignored -> ResponseEntity.noContent().build();
            case Result.Failure<Integer, ApplicationError> failure ->
                    ErrorResponseAssembler.toErrorResponseFromApplicationError(failure.error());
        };
    }

    @DeleteMapping("/{alertId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> clearAlert(
            Authentication authentication,
            @PathVariable Long alertId) {
        var adminUserId = resolveAdminUserId(authentication);
        if (adminUserId == 0L) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(
                    ApplicationError.notFound("Admin", authentication.getName()));
        }
        var result = alertCommandService.handle(new ClearAlertCommand(alertId, adminUserId));
        return switch (result) {
            case Result.Success<Alert, ApplicationError> ignored -> ResponseEntity.noContent().build();
            case Result.Failure<Alert, ApplicationError> failure ->
                    ErrorResponseAssembler.toErrorResponseFromApplicationError(failure.error());
        };
    }

    private Long resolveAdminUserId(Authentication authentication) {
        return iamContextFacade.fetchUserIdByUsername(authentication.getName()).orElse(0L);
    }

    private static <E extends Enum<E>> E parseEnum(Class<E> type, String parameter, String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            return Enum.valueOf(type, raw.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("%s must be one of %s but was '%s'."
                    .formatted(parameter, Arrays.toString(type.getEnumConstants()), raw));
        }
    }
}
