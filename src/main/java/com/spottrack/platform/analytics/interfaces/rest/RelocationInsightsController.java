package com.spottrack.platform.analytics.interfaces.rest;

import com.spottrack.platform.analytics.application.queryservices.RelocationInsightsQueryService;
import com.spottrack.platform.analytics.domain.model.queries.GetRelocationInsightsQuery;
import com.spottrack.platform.analytics.interfaces.rest.transform.RelocationInsightsResourceFromValueAssembler;
import com.spottrack.platform.gym.interfaces.acl.GymContextFacade;
import com.spottrack.platform.iam.interfaces.acl.IamContextFacade;
import com.spottrack.platform.shared.application.result.ApplicationError;
import com.spottrack.platform.shared.interfaces.rest.transform.ErrorResponseAssembler;
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

    @GetMapping("/gyms/{gymId}/relocation-insights")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getRelocationInsights(Authentication authentication,
                                                   @PathVariable String gymId,
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
