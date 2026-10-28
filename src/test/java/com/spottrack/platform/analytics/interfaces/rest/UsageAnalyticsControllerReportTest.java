package com.spottrack.platform.analytics.interfaces.rest;

import com.spottrack.platform.analytics.application.outboundservices.UsageReportRenderer;
import com.spottrack.platform.analytics.application.queryservices.UsageAnalyticsQueryService;
import com.spottrack.platform.analytics.application.queryservices.UsageReportQueryService;
import com.spottrack.platform.analytics.domain.model.queries.GetUsageReportQuery;
import com.spottrack.platform.analytics.domain.model.valueobjects.UsageHeatmap;
import com.spottrack.platform.analytics.domain.model.valueobjects.UsageReport;
import com.spottrack.platform.analytics.infrastructure.reporting.OpenPdfUsageReportRenderer;
import com.spottrack.platform.gym.interfaces.acl.GymContextFacade;
import com.spottrack.platform.iam.interfaces.acl.IamContextFacade;
import com.spottrack.platform.profiles.interfaces.acl.ProfilesContextFacade;
import com.spottrack.platform.shared.interfaces.rest.resources.ErrorResource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UsageAnalyticsControllerReportTest {

    private static final String GYM_ID = "gym-san-isidro";
    private static final String BRANCH_ID = "branch-1";
    private static final String ADMIN_EMAIL = "admin@mail.com";
    private static final LocalDate FROM = LocalDate.of(2026, 10, 1);
    private static final LocalDate TO = LocalDate.of(2026, 10, 28);

    private UsageReportQueryService reportQueryService;
    private UsageReportRenderer renderer;
    private GymContextFacade gymContextFacade;
    private IamContextFacade iamContextFacade;
    private ProfilesContextFacade profilesContextFacade;
    private UsageAnalyticsController controller;

    @BeforeEach
    void setUp() {
        reportQueryService = mock(UsageReportQueryService.class);
        renderer = mock(UsageReportRenderer.class);
        gymContextFacade = mock(GymContextFacade.class);
        iamContextFacade = mock(IamContextFacade.class);
        profilesContextFacade = mock(ProfilesContextFacade.class);
        controller = new UsageAnalyticsController(mock(UsageAnalyticsQueryService.class), reportQueryService, renderer,
                gymContextFacade, iamContextFacade, profilesContextFacade);
        when(iamContextFacade.fetchUserIdByUsername(ADMIN_EMAIL)).thenReturn(Optional.of(42L));
        when(gymContextFacade.isGymOwnedByAdmin(GYM_ID, 42L)).thenReturn(true);
    }

    @Test
    void ownerAdminDownloadsARealPdfWithAttachmentHeaders() {
        var realController = new UsageAnalyticsController(mock(UsageAnalyticsQueryService.class), reportQueryService,
                new OpenPdfUsageReportRenderer(), gymContextFacade, iamContextFacade, profilesContextFacade);
        when(reportQueryService.handle(any(GetUsageReportQuery.class))).thenReturn(report("Gym Ñuñoa Central", null));

        var response = realController.getGymUsageReport(admin(), GYM_ID, FROM, TO);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(MediaType.APPLICATION_PDF, response.getHeaders().getContentType());
        assertEquals("attachment; filename=\"spottrack-usage-gym-nunoa-central-2026-10-01_2026-10-28.pdf\"",
                response.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION));
        assertTrue(response.getHeaders().getCacheControl().contains("no-store"));
        var body = assertInstanceOf(byte[].class, response.getBody());
        assertEquals("%PDF", new String(body, 0, 4, StandardCharsets.ISO_8859_1));
        assertEquals(body.length, response.getHeaders().getContentLength());
    }

    @Test
    void branchReportIsNamedAfterTheBranchAndScopedToIt() {
        var pdf = new byte[]{'%', 'P', 'D', 'F'};
        when(gymContextFacade.resolveGymIdForBranch(BRANCH_ID)).thenReturn(Optional.of(GYM_ID));
        when(reportQueryService.handle(any(GetUsageReportQuery.class))).thenReturn(report("Gym Central", "Sede La Molina"));
        when(renderer.render(any(UsageReport.class))).thenReturn(pdf);

        var response = controller.getBranchUsageReport(admin(), BRANCH_ID, FROM, TO);

        assertEquals(200, response.getStatusCode().value());
        assertArrayEquals(pdf, (byte[]) response.getBody());
        assertEquals("attachment; filename=\"spottrack-usage-sede-la-molina-2026-10-01_2026-10-28.pdf\"",
                response.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION));
        var captor = ArgumentCaptor.forClass(GetUsageReportQuery.class);
        verify(reportQueryService).handle(captor.capture());
        assertEquals(GYM_ID, captor.getValue().gymId());
        assertEquals(BRANCH_ID, captor.getValue().branchId());
    }

    @Test
    void defaultsToTheLastSevenDays() {
        when(reportQueryService.handle(any(GetUsageReportQuery.class))).thenReturn(report("Gym", null));
        when(renderer.render(any(UsageReport.class))).thenReturn(new byte[]{1});

        controller.getGymUsageReport(admin(), GYM_ID, null, null);

        var captor = ArgumentCaptor.forClass(GetUsageReportQuery.class);
        verify(reportQueryService).handle(captor.capture());
        assertEquals(LocalDate.now(), captor.getValue().to());
        assertEquals(LocalDate.now().minusDays(6), captor.getValue().from());
    }

    @Test
    void adminThatDoesNotOwnTheGymGets403AsJson() {
        when(gymContextFacade.isGymOwnedByAdmin(GYM_ID, 42L)).thenReturn(false);

        var response = controller.getGymUsageReport(admin(), GYM_ID, FROM, TO);

        assertJsonError(response, 403, "FORBIDDEN");
        verify(reportQueryService, never()).handle(any(GetUsageReportQuery.class));
        verify(renderer, never()).render(any(UsageReport.class));
    }

    @Test
    void clientGets403EvenIfAssociatedWithTheGym() {
        when(profilesContextFacade.fetchClientIdByEmail("ana@mail.com")).thenReturn(7L);
        when(profilesContextFacade.hasAssociationWithGym(7L, GYM_ID)).thenReturn(true);

        var response = controller.getGymUsageReport(client(), GYM_ID, FROM, TO);

        assertJsonError(response, 403, "FORBIDDEN");
        verify(profilesContextFacade, never()).hasAssociationWithGym(any(), any());
        verify(renderer, never()).render(any(UsageReport.class));
    }

    @Test
    void invertedRangeGets400AsJson() {
        var response = controller.getGymUsageReport(admin(), GYM_ID, TO, FROM);

        assertEquals(400, response.getStatusCode().value());
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());
        assertInstanceOf(ErrorResource.class, response.getBody());
        verify(renderer, never()).render(any(UsageReport.class));
    }

    @Test
    void rangeLongerThan366DaysGets400() {
        var response = controller.getGymUsageReport(admin(), GYM_ID, LocalDate.of(2025, 1, 1), LocalDate.of(2026, 10, 28));

        assertEquals(400, response.getStatusCode().value());
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());
    }

    @Test
    void unknownBranchGets404AsJsonBeforeTheAccessCheck() {
        when(gymContextFacade.resolveGymIdForBranch("ghost")).thenReturn(Optional.empty());

        var response = controller.getBranchUsageReport(admin(), "ghost", FROM, TO);

        assertJsonError(response, 404, "BRANCH_NOT_FOUND");
        verify(gymContextFacade, never()).isGymOwnedByAdmin(any(), any());
    }

    @Test
    void endpointsAreAdminOnlyAndAdvertiseBothPdfAndJson() throws NoSuchMethodException {
        for (var method : List.of(
                UsageAnalyticsController.class.getMethod("getGymUsageReport", Authentication.class, String.class, LocalDate.class, LocalDate.class),
                UsageAnalyticsController.class.getMethod("getBranchUsageReport", Authentication.class, String.class, LocalDate.class, LocalDate.class))) {
            assertEquals("hasRole('ADMIN')", method.getAnnotation(PreAuthorize.class).value());
            var produces = List.of(method.getAnnotation(GetMapping.class).produces());
            assertTrue(produces.contains(MediaType.APPLICATION_PDF_VALUE));
            assertTrue(produces.contains(MediaType.APPLICATION_JSON_VALUE));
            assertTrue(method.getAnnotation(GetMapping.class).value()[0].endsWith("/report.pdf"));
        }
    }

    private static void assertJsonError(ResponseEntity<?> response, int status, String code) {
        assertEquals(status, response.getStatusCode().value());
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());
        assertEquals(code, assertInstanceOf(ErrorResource.class, response.getBody()).code());
    }

    private static UsageReport report(String gymName, String branchName) {
        var heatmap = new UsageHeatmap(FROM, TO, 6, 22, 0, 0, 0, null, 0, List.of(), List.of());
        return new UsageReport(gymName, branchName, LocalDateTime.of(2026, 10, 28, 9, 30), heatmap, Map.of());
    }

    private static Authentication admin() {
        return new UsernamePasswordAuthenticationToken(ADMIN_EMAIL, null, List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
    }

    private static Authentication client() {
        return new UsernamePasswordAuthenticationToken("ana@mail.com", null, List.of(new SimpleGrantedAuthority("ROLE_CLIENT")));
    }
}
