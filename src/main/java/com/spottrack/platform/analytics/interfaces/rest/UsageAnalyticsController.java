package com.spottrack.platform.analytics.interfaces.rest;

import com.spottrack.platform.analytics.application.queryservices.UsageAnalyticsQueryService;
import com.spottrack.platform.analytics.domain.model.queries.GetUsageHeatmapQuery;
import com.spottrack.platform.analytics.interfaces.rest.transform.UsageHeatmapResourceFromValueAssembler;
import com.spottrack.platform.gym.interfaces.acl.GymContextFacade;
import com.spottrack.platform.iam.interfaces.acl.IamContextFacade;
import com.spottrack.platform.shared.application.result.ApplicationError;
import com.spottrack.platform.shared.interfaces.rest.transform.ErrorResponseAssembler;
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
 * default to the last 7 days.
 */
@RestController
@RequestMapping(value = "/api/v1/usage-analytics", produces = MediaType.APPLICATION_JSON_VALUE)
public class UsageAnalyticsController {

    private static final int MAX_RANGE_DAYS = 366;

    private final UsageAnalyticsQueryService usageAnalyticsQueryService;
    private final GymContextFacade gymContextFacade;
    private final IamContextFacade iamContextFacade;

    public UsageAnalyticsController(UsageAnalyticsQueryService usageAnalyticsQueryService,
                                    GymContextFacade gymContextFacade,
                                    IamContextFacade iamContextFacade) {
        this.usageAnalyticsQueryService = usageAnalyticsQueryService;
        this.gymContextFacade = gymContextFacade;
        this.iamContextFacade = iamContextFacade;
    }

    @GetMapping("/gyms/{gymId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getGymUsage(Authentication authentication, @PathVariable String gymId,
                                         @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                                         @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return heatmap(authentication, gymId, null, from, to);
    }

    @GetMapping("/branches/{branchId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getBranchUsage(Authentication authentication, @PathVariable String branchId,
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
        var adminUserId = iamContextFacade.fetchUserIdByUsername(authentication.getName()).orElse(0L);
        if (!gymContextFacade.isGymOwnedByAdmin(gymId, adminUserId)) {
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
}
