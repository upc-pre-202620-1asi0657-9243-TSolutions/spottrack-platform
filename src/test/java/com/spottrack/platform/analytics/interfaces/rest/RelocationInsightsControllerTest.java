package com.spottrack.platform.analytics.interfaces.rest;

import com.spottrack.platform.analytics.application.queryservices.RelocationInsightsQueryService;
import com.spottrack.platform.analytics.domain.model.queries.GetRelocationInsightsQuery;
import com.spottrack.platform.analytics.domain.model.valueobjects.BranchDemand;
import com.spottrack.platform.analytics.domain.model.valueobjects.DemandClassification;
import com.spottrack.platform.analytics.domain.model.valueobjects.RelocationInsights;
import com.spottrack.platform.analytics.domain.model.valueobjects.RelocationSuggestion;
import com.spottrack.platform.analytics.interfaces.rest.resources.RelocationInsightsResource;
import com.spottrack.platform.gym.interfaces.acl.GymContextFacade;
import com.spottrack.platform.iam.interfaces.acl.IamContextFacade;
import com.spottrack.platform.shared.interfaces.rest.resources.ErrorResource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RelocationInsightsControllerTest {

    private static final String GYM_ID = "gym-lima";
    private static final String ADMIN_EMAIL = "admin@mail.com";
    private static final LocalDate FROM = LocalDate.of(2026, 10, 1);
    private static final LocalDate TO = LocalDate.of(2026, 10, 28);

    private RelocationInsightsQueryService queryService;
    private GymContextFacade gymContextFacade;
    private RelocationInsightsController controller;

    @BeforeEach
    void setUp() {
        queryService = mock(RelocationInsightsQueryService.class);
        gymContextFacade = mock(GymContextFacade.class);
        var iamContextFacade = mock(IamContextFacade.class);
        controller = new RelocationInsightsController(queryService, gymContextFacade, iamContextFacade);
        when(iamContextFacade.fetchUserIdByUsername(ADMIN_EMAIL)).thenReturn(Optional.of(42L));
        when(gymContextFacade.isGymOwnedByAdmin(GYM_ID, 42L)).thenReturn(true);
        when(queryService.handle(any(GetRelocationInsightsQuery.class))).thenReturn(insights());
    }

    @Test
    void ownerAdminGetsTheInsightsOfTheRequestedRange() {
        var response = controller.getRelocationInsights(admin(), GYM_ID, FROM, TO);

        assertEquals(200, response.getStatusCode().value());
        var body = assertInstanceOf(RelocationInsightsResource.class, response.getBody());
        assertEquals("Lima", body.gymName());
        assertEquals(20.0, body.thresholds().lowDemandRate());
        assertEquals(50.0, body.thresholds().saturatedRate());
        assertEquals("LOW_DEMAND", body.branches().get(0).classification());
        assertEquals("Treadmill", body.suggestions().get(0).equipmentName());
        assertEquals("b2", body.suggestions().get(0).toBranchId());
        var captor = ArgumentCaptor.forClass(GetRelocationInsightsQuery.class);
        verify(queryService).handle(captor.capture());
        assertEquals(new GetRelocationInsightsQuery(GYM_ID, FROM, TO), captor.getValue());
    }

    @Test
    void defaultsToTheLastSevenDays() {
        controller.getRelocationInsights(admin(), GYM_ID, null, null);

        var captor = ArgumentCaptor.forClass(GetRelocationInsightsQuery.class);
        verify(queryService).handle(captor.capture());
        assertEquals(LocalDate.now(), captor.getValue().to());
        assertEquals(LocalDate.now().minusDays(6), captor.getValue().from());
    }

    @Test
    void acceptsARangeOf366Days() {
        var response = controller.getRelocationInsights(admin(), GYM_ID, TO.minusDays(365), TO);

        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    void adminThatDoesNotOwnTheGymGets403() {
        when(gymContextFacade.isGymOwnedByAdmin(GYM_ID, 42L)).thenReturn(false);

        assertError(controller.getRelocationInsights(admin(), GYM_ID, FROM, TO), 403, "FORBIDDEN");
        verify(queryService, never()).handle(any());
    }

    @Test
    void unknownGymGets403LikeTheOtherUsageEndpoints() {
        assertError(controller.getRelocationInsights(admin(), "ghost", FROM, TO), 403, "FORBIDDEN");
        verify(queryService, never()).handle(any());
    }

    @Test
    void clientGets403() {
        assertError(controller.getRelocationInsights(client(), GYM_ID, FROM, TO), 403, "FORBIDDEN");
        verify(queryService, never()).handle(any());
    }

    @Test
    void invertedRangeGets400() {
        assertError(controller.getRelocationInsights(admin(), GYM_ID, TO, FROM), 400, "VALIDATION_ERROR");
        verify(queryService, never()).handle(any());
    }

    @Test
    void rangeLongerThan366DaysGets400() {
        assertError(controller.getRelocationInsights(admin(), GYM_ID, TO.minusDays(366), TO), 400, "VALIDATION_ERROR");
        verify(queryService, never()).handle(any());
    }

    @Test
    void accessIsCheckedBeforeTheRange() {
        when(gymContextFacade.isGymOwnedByAdmin(GYM_ID, 42L)).thenReturn(false);

        assertError(controller.getRelocationInsights(admin(), GYM_ID, TO, FROM), 403, "FORBIDDEN");
    }

    @Test
    void endpointIsAdminOnlyAndLivesUnderTheUsageAnalyticsPrefix() throws NoSuchMethodException {
        var method = RelocationInsightsController.class.getMethod(
                "getRelocationInsights", Authentication.class, String.class, LocalDate.class, LocalDate.class);

        assertEquals("hasRole('ADMIN')", method.getAnnotation(PreAuthorize.class).value());
        assertEquals("/gyms/{gymId}/relocation-insights", method.getAnnotation(GetMapping.class).value()[0]);
        assertEquals("/api/v1/usage-analytics",
                RelocationInsightsController.class.getAnnotation(RequestMapping.class).value()[0]);
    }

    private static void assertError(ResponseEntity<?> response, int status, String code) {
        assertEquals(status, response.getStatusCode().value());
        assertEquals(code, assertInstanceOf(ErrorResource.class, response.getBody()).code());
    }

    private static RelocationInsights insights() {
        var idle = new BranchDemand("b1", "Idle", 4, 120, 8.0, DemandClassification.LOW_DEMAND);
        var busy = new BranchDemand("b2", "Busy", 4, 5000, 72.0, DemandClassification.SATURATED);
        var suggestion = new RelocationSuggestion("e1", "Treadmill", "b1", "Idle", 8.0, "b2", "Busy", 72.0, 1.5, 12, "reason");
        return new RelocationInsights(GYM_ID, "Lima", FROM, TO, 20, 50, List.of(idle, busy), List.of(suggestion));
    }

    private static Authentication admin() {
        return new UsernamePasswordAuthenticationToken(ADMIN_EMAIL, null, List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
    }

    private static Authentication client() {
        return new UsernamePasswordAuthenticationToken("ana@mail.com", null, List.of(new SimpleGrantedAuthority("ROLE_CLIENT")));
    }
}
