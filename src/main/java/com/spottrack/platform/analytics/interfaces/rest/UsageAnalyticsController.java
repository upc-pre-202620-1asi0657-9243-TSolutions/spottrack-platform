package com.spottrack.platform.analytics.interfaces.rest;

import com.spottrack.platform.analytics.application.queryservices.UsageAnalyticsQueryService;
import com.spottrack.platform.analytics.domain.model.queries.GetUsageHeatmapQuery;
import com.spottrack.platform.analytics.interfaces.rest.resources.UsageHeatmapResource;
import com.spottrack.platform.analytics.interfaces.rest.transform.UsageHeatmapResourceFromValueAssembler;
import com.spottrack.platform.gym.interfaces.acl.GymContextFacade;
import com.spottrack.platform.iam.interfaces.acl.IamContextFacade;
import com.spottrack.platform.profiles.interfaces.acl.ProfilesContextFacade;
import com.spottrack.platform.shared.application.result.ApplicationError;
import com.spottrack.platform.shared.interfaces.rest.resources.ErrorResource;
import com.spottrack.platform.shared.interfaces.rest.transform.ErrorResponseAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Occupancy and heat built from real sensor sessions. Dates are inclusive and
 * default to the last 7 days. Readable by the admin who owns the gym and by
 * clients associated with it, whether or not it is their active gym (the client map shows the heat).
 */
@RestController
@Tag(name = "usage-analytics")
@RequestMapping(value = "/api/v1/usage-analytics", produces = MediaType.APPLICATION_JSON_VALUE)
public class UsageAnalyticsController {

    private static final int MAX_RANGE_DAYS = 366;

    private final UsageAnalyticsQueryService usageAnalyticsQueryService;
    private final GymContextFacade gymContextFacade;
    private final IamContextFacade iamContextFacade;
    private final ProfilesContextFacade profilesContextFacade;

    public UsageAnalyticsController(UsageAnalyticsQueryService usageAnalyticsQueryService,
                                    GymContextFacade gymContextFacade,
                                    IamContextFacade iamContextFacade,
                                    ProfilesContextFacade profilesContextFacade) {
        this.usageAnalyticsQueryService = usageAnalyticsQueryService;
        this.gymContextFacade = gymContextFacade;
        this.iamContextFacade = iamContextFacade;
        this.profilesContextFacade = profilesContextFacade;
    }

    @Operation(
            summary = "Usage heatmap of a gym",
            description = "Occupancy and per-equipment heat of the whole gym. Dates are inclusive (yyyy-MM-dd) and default to the last 7 days; the range may span at most 366 days. "
                    + "Clients may read any gym they are associated with, not only their active gym, as long as their DNI is still whitelisted there. "
                    + "Admins may read gyms they own."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usage heatmap",
                    content = @Content(schema = @Schema(implementation = UsageHeatmapResource.class))),
            @ApiResponse(responseCode = "400", description = "Invalid date range (from after to, or more than 366 days)",
                    content = @Content(schema = @Schema(implementation = ErrorResource.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid authentication token",
                    content = @Content(schema = @Schema(implementation = ErrorResource.class))),
            @ApiResponse(responseCode = "403", description = "Caller is not allowed to read this gym: an admin who does not own it, "
                    + "or a client that is not associated with it or has no profile (also returned when the gym does not exist)",
                    content = @Content(schema = @Schema(implementation = ErrorResource.class)))
    })
    @GetMapping("/gyms/{gymId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CLIENT')")
    public ResponseEntity<?> getGymUsage(Authentication authentication,
                                         @Parameter(description = "Gym unique identifier (UUID)", required = true) @PathVariable String gymId,
                                         @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                                         @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return heatmap(authentication, gymId, null, from, to);
    }

    @Operation(
            summary = "Usage heatmap of a branch",
            description = "Occupancy and per-equipment heat of one branch. Dates are inclusive (yyyy-MM-dd) and default to the last 7 days; the range may span at most 366 days. "
                    + "Clients may read any gym they are associated with, not only their active gym, as long as their DNI is still whitelisted there. "
                    + "Admins may read gyms they own."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usage heatmap",
                    content = @Content(schema = @Schema(implementation = UsageHeatmapResource.class))),
            @ApiResponse(responseCode = "400", description = "Invalid date range (from after to, or more than 366 days)",
                    content = @Content(schema = @Schema(implementation = ErrorResource.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid authentication token",
                    content = @Content(schema = @Schema(implementation = ErrorResource.class))),
            @ApiResponse(responseCode = "403", description = "Caller is not allowed to read this gym: an admin who does not own it, "
                    + "or a client that is not associated with it or has no profile (also returned when the gym does not exist)",
                    content = @Content(schema = @Schema(implementation = ErrorResource.class))),
            @ApiResponse(responseCode = "404", description = "Branch does not exist (checked before access)",
                    content = @Content(schema = @Schema(implementation = ErrorResource.class)))
    })
    @GetMapping("/branches/{branchId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CLIENT')")
    public ResponseEntity<?> getBranchUsage(Authentication authentication,
                                            @Parameter(description = "Branch unique identifier (UUID)", required = true) @PathVariable String branchId,
                                            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                                            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        var gymId = gymContextFacade.resolveGymIdForBranch(branchId);
        if (gymId.isEmpty()) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(ApplicationError.notFound("Branch", branchId));
        }
        return heatmap(authentication, gymId.get(), branchId, from, to);
    }

    private ResponseEntity<?> heatmap(Authentication authentication, String gymId, String branchId,
                                      LocalDate from, LocalDate to) {
        if (!canReadGym(authentication, gymId)) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(ApplicationError.forbidden("Gym", "gymId:" + gymId));
        }
        var end = to != null ? to : LocalDate.now();
        var start = from != null ? from : end.minusDays(6);
        if (end.isBefore(start) || ChronoUnit.DAYS.between(start, end) >= MAX_RANGE_DAYS) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(ApplicationError.validationError(
                    "dateRange", "from must be on or before to, spanning at most " + MAX_RANGE_DAYS + " days"));
        }
        var heatmap = usageAnalyticsQueryService.handle(new GetUsageHeatmapQuery(gymId, branchId, start, end));
        return ResponseEntity.ok(UsageHeatmapResourceFromValueAssembler.toResourceFromValue(heatmap));
    }

    private boolean canReadGym(Authentication authentication, String gymId) {
        var username = authentication.getName();
        if (hasRole(authentication, "ROLE_ADMIN")) {
            var adminUserId = iamContextFacade.fetchUserIdByUsername(username).orElse(0L);
            return gymContextFacade.isGymOwnedByAdmin(gymId, adminUserId);
        }
        var clientId = profilesContextFacade.fetchClientIdByEmail(username);
        return clientId != null && clientId > 0 && profilesContextFacade.hasAssociationWithGym(clientId, gymId);
    }

    private static boolean hasRole(Authentication authentication, String role) {
        return authentication.getAuthorities().stream().anyMatch(a -> role.equals(a.getAuthority()));
    }
}
