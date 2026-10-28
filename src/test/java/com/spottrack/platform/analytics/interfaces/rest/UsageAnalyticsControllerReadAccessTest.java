package com.spottrack.platform.analytics.interfaces.rest;

import com.spottrack.platform.analytics.application.queryservices.UsageAnalyticsQueryService;
import com.spottrack.platform.analytics.domain.model.queries.GetUsageHeatmapQuery;
import com.spottrack.platform.analytics.domain.model.valueobjects.UsageHeatmap;
import com.spottrack.platform.gym.interfaces.acl.GymContextFacade;
import com.spottrack.platform.iam.interfaces.acl.IamContextFacade;
import com.spottrack.platform.profiles.interfaces.acl.ProfilesContextFacade;
import com.spottrack.platform.shared.interfaces.rest.resources.ErrorResource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

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

class UsageAnalyticsControllerReadAccessTest {

    private static final String GYM_ID = "gym-san-isidro";
    private static final String BRANCH_ID = "branch-1";
    private static final String CLIENT_EMAIL = "ana@mail.com";
    private static final Long CLIENT_ID = 7L;

    private UsageAnalyticsQueryService queryService;
    private GymContextFacade gymContextFacade;
    private IamContextFacade iamContextFacade;
    private ProfilesContextFacade profilesContextFacade;
    private UsageAnalyticsController controller;

    @BeforeEach
    void setUp() {
        queryService = mock(UsageAnalyticsQueryService.class);
        gymContextFacade = mock(GymContextFacade.class);
        iamContextFacade = mock(IamContextFacade.class);
        profilesContextFacade = mock(ProfilesContextFacade.class);
        controller = new UsageAnalyticsController(queryService, gymContextFacade, iamContextFacade, profilesContextFacade);
        when(queryService.handle(any(GetUsageHeatmapQuery.class))).thenReturn(heatmap());
    }

    @Test
    void clientReadsGymUsageOfAnAssociatedGymThatIsNotActive() {
        when(profilesContextFacade.fetchClientIdByEmail(CLIENT_EMAIL)).thenReturn(CLIENT_ID);
        when(profilesContextFacade.hasAssociationWithGym(CLIENT_ID, GYM_ID)).thenReturn(true);
        when(profilesContextFacade.hasActiveAssociationWithGym(CLIENT_ID, GYM_ID)).thenReturn(false);

        var response = controller.getGymUsage(client(), GYM_ID, null, null);

        assertEquals(200, response.getStatusCode().value());
        verify(queryService).handle(any(GetUsageHeatmapQuery.class));
    }

    @Test
    void clientReadsBranchUsageOfAnAssociatedGymThatIsNotActive() {
        when(gymContextFacade.resolveGymIdForBranch(BRANCH_ID)).thenReturn(Optional.of(GYM_ID));
        when(profilesContextFacade.fetchClientIdByEmail(CLIENT_EMAIL)).thenReturn(CLIENT_ID);
        when(profilesContextFacade.hasAssociationWithGym(CLIENT_ID, GYM_ID)).thenReturn(true);

        var response = controller.getBranchUsage(client(), BRANCH_ID, null, null);

        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    void clientGets403ForAGymItIsNotAssociatedWith() {
        when(profilesContextFacade.fetchClientIdByEmail(CLIENT_EMAIL)).thenReturn(CLIENT_ID);
        when(profilesContextFacade.hasAssociationWithGym(CLIENT_ID, GYM_ID)).thenReturn(false);

        var response = controller.getGymUsage(client(), GYM_ID, null, null);

        assertEquals(403, response.getStatusCode().value());
        assertEquals("FORBIDDEN", assertInstanceOf(ErrorResource.class, response.getBody()).code());
        verify(queryService, never()).handle(any(GetUsageHeatmapQuery.class));
    }

    @Test
    void clientWithoutProfileGets403() {
        when(profilesContextFacade.fetchClientIdByEmail(CLIENT_EMAIL)).thenReturn(0L);

        assertEquals(403, controller.getGymUsage(client(), GYM_ID, null, null).getStatusCode().value());
        verify(profilesContextFacade, never()).hasAssociationWithGym(any(), any());
    }

    @Test
    void unknownBranchGets404BeforeTheAccessCheck() {
        when(gymContextFacade.resolveGymIdForBranch("ghost")).thenReturn(Optional.empty());

        var response = controller.getBranchUsage(client(), "ghost", null, null);

        assertEquals(404, response.getStatusCode().value());
        assertEquals("BRANCH_NOT_FOUND", assertInstanceOf(ErrorResource.class, response.getBody()).code());
        verify(profilesContextFacade, never()).hasAssociationWithGym(any(), any());
    }

    @Test
    void adminOwnerReadsItsGymWithoutTheClientAssociationCheck() {
        when(iamContextFacade.fetchUserIdByUsername("admin@mail.com")).thenReturn(Optional.of(42L));
        when(gymContextFacade.isGymOwnedByAdmin(GYM_ID, 42L)).thenReturn(true);

        var response = controller.getGymUsage(admin(), GYM_ID, null, null);

        assertEquals(200, response.getStatusCode().value());
        verify(profilesContextFacade, never()).hasAssociationWithGym(any(), any());
    }

    @Test
    void adminThatDoesNotOwnTheGymGets403() {
        when(iamContextFacade.fetchUserIdByUsername("admin@mail.com")).thenReturn(Optional.of(99L));
        when(gymContextFacade.isGymOwnedByAdmin(GYM_ID, 99L)).thenReturn(false);

        assertEquals(403, controller.getGymUsage(admin(), GYM_ID, null, null).getStatusCode().value());
    }

    private static UsageHeatmap heatmap() {
        var day = LocalDate.of(2026, 1, 5);
        return new UsageHeatmap(day, day, 6, 22, 0, 0, 0, null, 0, List.of(), List.of());
    }

    private static Authentication client() {
        return new UsernamePasswordAuthenticationToken(CLIENT_EMAIL, null,
                List.of(new SimpleGrantedAuthority("ROLE_CLIENT")));
    }

    private static Authentication admin() {
        return new UsernamePasswordAuthenticationToken("admin@mail.com", null,
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
    }
}
