package com.spottrack.platform.analytics.interfaces.rest;

import com.spottrack.platform.analytics.application.queryservices.RelocationInsightsQueryService;
import com.spottrack.platform.analytics.domain.model.queries.GetRelocationInsightsQuery;
import com.spottrack.platform.analytics.interfaces.rest.resources.RelocationInsightsResource;
import com.spottrack.platform.analytics.interfaces.rest.transform.RelocationInsightsResourceFromValueAssembler;
import com.spottrack.platform.gym.interfaces.acl.GymContextFacade;
import com.spottrack.platform.iam.interfaces.acl.IamContextFacade;
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
 * Cross-branch demand comparison to spot machines worth moving from idle branches to saturated ones.
 * Restricted to the admin who owns the gym; same date rules as the usage heatmap and PDF report.
 */
@RestController
@Tag(name = "usage-analytics")
@RequestMapping(value = "/api/v1/usage-analytics", produces = MediaType.APPLICATION_JSON_VALUE)
public class RelocationInsightsController {

    private static final int MAX_RANGE_DAYS = 366;

    private final RelocationInsightsQueryService relocationInsightsQueryService;
    private final GymContextFacade gymContextFacade;
    private final IamContextFacade iamContextFacade;

    public RelocationInsightsController(RelocationInsightsQueryService relocationInsightsQueryService,
                                        GymContextFacade gymContextFacade,
                                        IamContextFacade iamContextFacade) {
        this.relocationInsightsQueryService = relocationInsightsQueryService;
        this.gymContextFacade = gymContextFacade;
        this.iamContextFacade = iamContextFacade;
    }

    @Operation(
            summary = "Relocation insights of a gym",
            description = "Compares the demand of the branches of a gym and suggests moving under-used machines from low-demand branches to saturated ones. "
                    + "Each branch gets its equipment count, total minutes of use and utilization rate (average % of its machines in use during opening hours, "
                    + "comparable across branches of any size) and a classification: LOW_DEMAND below the low-demand rate, SATURATED at or above the saturated rate, "
                    + "BALANCED otherwise (a branch without equipment is BALANCED). Thresholds default to 20% and 50% and are configurable with "
                    + "spottrack.analytics.relocation.low-demand-rate and spottrack.analytics.relocation.saturated-rate; they are echoed in the response. "
                    + "Suggestions pick the least-used machines (own utilization below the low-demand rate, not out of service or in maintenance) of LOW_DEMAND branches "
                    + "and spread them over the SATURATED branches, most saturated first. A branch never gives away more than half of its machines and at most "
                    + "spottrack.analytics.relocation.max-suggestions (default 10) are returned. The suggestion names the target branch only; relocate the machine to one of its zones with PATCH /api/v1/equipments/{equipmentId}/relocate. "
                    + "Limitation: usage is attributed to the branch where a machine is now, so the history of a machine that was relocated inside the window moves with it. "
                    + "Dates are inclusive (yyyy-MM-dd) and default to the last 7 days; the range may span at most 366 days. Only the admin who owns the gym may read it."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Branch demand and relocation suggestions (empty lists when there is nothing to compare)",
                    content = @Content(schema = @Schema(implementation = RelocationInsightsResource.class))),
            @ApiResponse(responseCode = "400", description = "Invalid date range (from after to, or more than 366 days)",
                    content = @Content(schema = @Schema(implementation = ErrorResource.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid authentication token",
                    content = @Content(schema = @Schema(implementation = ErrorResource.class))),
            @ApiResponse(responseCode = "403", description = "Caller is not the admin who owns the gym (also returned for clients and when the gym does not exist)",
                    content = @Content(schema = @Schema(implementation = ErrorResource.class)))
    })
    @GetMapping("/gyms/{gymId}/relocation-insights")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getRelocationInsights(Authentication authentication,
                                                   @Parameter(description = "Gym unique identifier (UUID)", required = true) @PathVariable String gymId,
                                                   @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                                                   @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        if (!isOwningAdmin(authentication, gymId)) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(ApplicationError.forbidden("Gym", "gymId:" + gymId));
        }
        var end = to != null ? to : LocalDate.now();
        var start = from != null ? from : end.minusDays(6);
        if (end.isBefore(start) || ChronoUnit.DAYS.between(start, end) >= MAX_RANGE_DAYS) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(ApplicationError.validationError(
                    "dateRange", "from must be on or before to, spanning at most " + MAX_RANGE_DAYS + " days"));
        }
        var insights = relocationInsightsQueryService.handle(new GetRelocationInsightsQuery(gymId, start, end));
        return ResponseEntity.ok(RelocationInsightsResourceFromValueAssembler.toResourceFromValue(insights));
    }

    private boolean isOwningAdmin(Authentication authentication, String gymId) {
        if (authentication.getAuthorities().stream().noneMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()))) {
            return false;
        }
        var adminUserId = iamContextFacade.fetchUserIdByUsername(authentication.getName()).orElse(0L);
        return gymContextFacade.isGymOwnedByAdmin(gymId, adminUserId);
    }
}
