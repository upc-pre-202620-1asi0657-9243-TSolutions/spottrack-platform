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
import com.spottrack.platform.shared.interfaces.rest.resources.ErrorResource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AlertsControllerTest {

    private static final String ADMIN_EMAIL = "admin@gym.com";
    private static final String CLIENT_EMAIL = "ana@mail.com";
    private static final Long ADMIN_ID = 42L;
    private static final Long CLIENT_ID = 7L;
    private static final Date CREATED = new Date(1_000L);
    private static final Date RESOLVED = new Date(2_000L);

    private AlertCommandService commandService;
    private AlertQueryService queryService;
    private IamContextFacade iamContextFacade;
    private AlertsController controller;

    @BeforeEach
    void setUp() {
        commandService = mock(AlertCommandService.class);
        queryService = mock(AlertQueryService.class);
        iamContextFacade = mock(IamContextFacade.class);
        controller = new AlertsController(commandService, queryService, iamContextFacade);
        when(iamContextFacade.fetchUserIdByUsername(ADMIN_EMAIL)).thenReturn(Optional.of(ADMIN_ID));
        when(iamContextFacade.fetchUserIdByUsername(CLIENT_EMAIL)).thenReturn(Optional.of(CLIENT_ID));
        when(queryService.handle(any(GetAlertsByAdminUserIdQuery.class))).thenReturn(List.of());
    }

    private static Alert alert(boolean resolved) {
        return new Alert(1L, ADMIN_ID, "eq-1", AlertSeverity.WARNING, AlertSource.SENSOR, "msg",
                resolved, resolved ? RESOLVED : null, null, CREATED);
    }

    private GetAlertsByAdminUserIdQuery capturedQuery() {
        var captor = ArgumentCaptor.forClass(GetAlertsByAdminUserIdQuery.class);
        verify(queryService).handle(captor.capture());
        return captor.getValue();
    }

    private static ErrorResource errorBody(org.springframework.http.ResponseEntity<?> response) {
        return assertInstanceOf(ErrorResource.class, response.getBody());
    }

    @Test
    void listDefaultsToPendingAlertsWithNoOtherFilter() {
        var response = controller.getAlerts(admin(), null, null, null);

        assertEquals(200, response.getStatusCode().value());
        var query = capturedQuery();
        assertEquals(ADMIN_ID, query.adminUserId());
        assertEquals(AlertStatusFilter.PENDING, query.status());
        assertNull(query.source());
        assertNull(query.severity());
    }

    @Test
    void listPassesTheFiltersCaseInsensitively() {
        controller.getAlerts(admin(), "resolved", "Sensor", "critical");

        var query = capturedQuery();
        assertEquals(AlertStatusFilter.RESOLVED, query.status());
        assertEquals(AlertSource.SENSOR, query.source());
        assertEquals(AlertSeverity.CRITICAL, query.severity());
    }

    @Test
    void listMapsAlertsToResourcesWithSourceAndResolvedAt() {
        when(queryService.handle(any(GetAlertsByAdminUserIdQuery.class))).thenReturn(List.of(alert(true)));

        var response = controller.getAlerts(admin(), "ALL", null, null);

        var resources = (List<?>) response.getBody();
        var resource = assertInstanceOf(AlertResource.class, resources.getFirst());
        assertEquals("SENSOR", resource.source());
        assertEquals(RESOLVED, resource.resolvedAt());
        assertTrue(resource.isResolved());
    }

    @Test
    void listWithAnInvalidStatusIs400WithPlainTextDetails() {
        var response = controller.getAlerts(admin(), "DONE", null, null);

        assertEquals(400, response.getStatusCode().value());
        var error = errorBody(response);
        assertEquals("VALIDATION_ERROR", error.code());
        assertEquals("status must be one of [PENDING, RESOLVED, ALL] but was 'DONE'.", error.details());
        verify(queryService, never()).handle(any(GetAlertsByAdminUserIdQuery.class));
    }

    @Test
    void listWithAnInvalidSourceOrSeverityIs400() {
        var badSource = controller.getAlerts(admin(), null, "RESERVATION", null);
        var badSeverity = controller.getAlerts(admin(), null, null, "INFO");

        assertEquals(400, badSource.getStatusCode().value());
        assertEquals("source must be one of [MAINTENANCE, SENSOR, ANOMALY] but was 'RESERVATION'.", errorBody(badSource).details());
        assertEquals(400, badSeverity.getStatusCode().value());
        assertEquals("severity must be one of [WARNING, CRITICAL] but was 'INFO'.", errorBody(badSeverity).details());
    }

    @Test
    void listForAnUnknownUserIs404() {
        when(iamContextFacade.fetchUserIdByUsername("ghost@mail.com")).thenReturn(Optional.empty());

        var response = controller.getAlerts(auth("ghost@mail.com", "ROLE_ADMIN"), null, null, null);

        assertEquals(404, response.getStatusCode().value());
        assertEquals("ADMIN_NOT_FOUND", errorBody(response).code());
    }

    @Test
    void listIsOpenToAnyAuthenticatedUserAndScopedToTheCallersOwnAlerts() {
        var response = controller.getAlerts(auth(CLIENT_EMAIL, "ROLE_CLIENT"), null, null, null);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(CLIENT_ID, capturedQuery().adminUserId());
        assertNull(preAuthorize("getAlerts"));
    }

    @Test
    void resolveReturnsTheResolvedAlert() {
        when(commandService.handle(new ResolveAlertCommand(1L, ADMIN_ID))).thenReturn(Result.success(alert(true)));

        var response = controller.resolveAlert(admin(), 1L);

        assertEquals(200, response.getStatusCode().value());
        var resource = assertInstanceOf(AlertResource.class, response.getBody());
        assertTrue(resource.isResolved());
        assertEquals(RESOLVED, resource.resolvedAt());
    }

    @Test
    void resolveOfAnotherAdminsAlertIs403AndUnknownIs404() {
        when(commandService.handle(new ResolveAlertCommand(1L, ADMIN_ID)))
                .thenReturn(Result.failure(ApplicationError.forbidden("Alert", "alertId:1")));
        when(commandService.handle(new ResolveAlertCommand(2L, ADMIN_ID)))
                .thenReturn(Result.failure(ApplicationError.notFound("Alert", "2")));

        assertEquals(403, controller.resolveAlert(admin(), 1L).getStatusCode().value());
        var notFound = controller.resolveAlert(admin(), 2L);
        assertEquals(404, notFound.getStatusCode().value());
        assertEquals("ALERT_NOT_FOUND", errorBody(notFound).code());
    }

    @Test
    void clearAlertReturns204() {
        when(commandService.handle(new ClearAlertCommand(1L, ADMIN_ID))).thenReturn(Result.success(alert(true)));

        var response = controller.clearAlert(admin(), 1L);

        assertEquals(204, response.getStatusCode().value());
        assertNull(response.getBody());
    }

    @Test
    void clearOfAPendingAlertIs422WithPlainTextDetails() {
        when(commandService.handle(new ClearAlertCommand(1L, ADMIN_ID))).thenReturn(Result.failure(
                ApplicationError.businessRuleViolation("Alert clearing",
                        "Only resolved alerts can be cleared; resolve the alert first.")));

        var response = controller.clearAlert(admin(), 1L);

        assertEquals(422, response.getStatusCode().value());
        var error = errorBody(response);
        assertEquals("BUSINESS_RULE_VIOLATION", error.code());
        assertEquals("Only resolved alerts can be cleared; resolve the alert first.", error.details());
    }

    @Test
    void clearOfAnotherAdminsAlertIs403AndUnknownIs404() {
        when(commandService.handle(new ClearAlertCommand(1L, ADMIN_ID)))
                .thenReturn(Result.failure(ApplicationError.forbidden("Alert", "alertId:1")));
        when(commandService.handle(new ClearAlertCommand(2L, ADMIN_ID)))
                .thenReturn(Result.failure(ApplicationError.notFound("Alert", "2")));

        assertEquals(403, controller.clearAlert(admin(), 1L).getStatusCode().value());
        assertEquals(404, controller.clearAlert(admin(), 2L).getStatusCode().value());
    }

    @Test
    void clearResolvedReturns204AndClearsTheCallersAlerts() {
        when(commandService.handle(new ClearResolvedAlertsCommand(ADMIN_ID))).thenReturn(Result.success(3));

        var response = controller.clearResolvedAlerts(admin());

        assertEquals(204, response.getStatusCode().value());
        verify(commandService).handle(new ClearResolvedAlertsCommand(ADMIN_ID));
    }

    @Test
    void clearResolvedWithNothingToClearIsStill204() {
        when(commandService.handle(new ClearResolvedAlertsCommand(ADMIN_ID))).thenReturn(Result.success(0));

        assertEquals(204, controller.clearResolvedAlerts(admin()).getStatusCode().value());
    }

    @Test
    void mutatingEndpointsForAnUnknownUserAre404WithoutTouchingTheServices() {
        when(iamContextFacade.fetchUserIdByUsername("ghost@mail.com")).thenReturn(Optional.empty());
        var ghost = auth("ghost@mail.com", "ROLE_ADMIN");

        assertEquals(404, controller.resolveAlert(ghost, 1L).getStatusCode().value());
        assertEquals(404, controller.clearAlert(ghost, 1L).getStatusCode().value());
        assertEquals(404, controller.clearResolvedAlerts(ghost).getStatusCode().value());
        verify(commandService, never()).handle(any(ResolveAlertCommand.class));
        verify(commandService, never()).handle(any(ClearAlertCommand.class));
        verify(commandService, never()).handle(any(ClearResolvedAlertsCommand.class));
    }

    @Test
    void resolveAndBothDeleteEndpointsAreAdminOnly() {
        assertEquals("hasRole('ADMIN')", preAuthorize("resolveAlert"));
        assertEquals("hasRole('ADMIN')", preAuthorize("clearAlert"));
        assertEquals("hasRole('ADMIN')", preAuthorize("clearResolvedAlerts"));
    }

    private static String preAuthorize(String methodName) {
        var method = Arrays.stream(AlertsController.class.getDeclaredMethods())
                .filter(m -> m.getName().equals(methodName))
                .findFirst()
                .orElseThrow();
        var annotation = method.getAnnotation(PreAuthorize.class);
        return annotation == null ? null : annotation.value();
    }

    private static Authentication admin() {
        return auth(ADMIN_EMAIL, "ROLE_ADMIN");
    }

    private static Authentication auth(String username, String role) {
        return new UsernamePasswordAuthenticationToken(username, null, List.of(new SimpleGrantedAuthority(role)));
    }
}
