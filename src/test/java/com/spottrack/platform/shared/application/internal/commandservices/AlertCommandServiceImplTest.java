package com.spottrack.platform.shared.application.internal.commandservices;

import com.spottrack.platform.shared.application.result.ApplicationError;
import com.spottrack.platform.shared.application.result.Result;
import com.spottrack.platform.shared.domain.model.aggregates.Alert;
import com.spottrack.platform.shared.domain.model.commands.ClearAlertCommand;
import com.spottrack.platform.shared.domain.model.commands.ClearResolvedAlertsCommand;
import com.spottrack.platform.shared.domain.model.commands.CreateAlertCommand;
import com.spottrack.platform.shared.domain.model.commands.ResolveAlertCommand;
import com.spottrack.platform.shared.domain.model.valueobjects.AlertSeverity;
import com.spottrack.platform.shared.domain.model.valueobjects.AlertSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AlertCommandServiceImplTest {

    private static final Long ADMIN = 42L;
    private static final Long OTHER_ADMIN = 99L;

    private InMemoryAlertRepository repository;
    private AlertCommandServiceImpl service;

    @BeforeEach
    void setUp() {
        repository = new InMemoryAlertRepository();
        service = new AlertCommandServiceImpl(repository);
    }

    private Alert create(Long adminId, AlertSource source) {
        var result = service.handle(new CreateAlertCommand(adminId, "eq-1", AlertSeverity.WARNING, source, "msg"));
        return success(result);
    }

    private Alert createResolved(Long adminId) {
        var alert = create(adminId, AlertSource.SENSOR);
        success(service.handle(new ResolveAlertCommand(alert.getId(), adminId)));
        return alert;
    }

    private static <T> T success(Result<T, ApplicationError> result) {
        return switch (result) {
            case Result.Success<T, ApplicationError> s -> s.value();
            case Result.Failure<T, ApplicationError> f -> throw new AssertionError("Expected success but was " + f.error());
        };
    }

    private static ApplicationError error(Result<?, ApplicationError> result) {
        if (result instanceof Result.Failure<?, ApplicationError> f) {
            return f.error();
        }
        throw new AssertionError("Expected a failure but the command succeeded");
    }

    @Test
    void createsAPendingAlertWithItsSource() {
        var alert = create(ADMIN, AlertSource.ANOMALY);

        assertEquals(AlertSource.ANOMALY, alert.getSource());
        assertFalse(alert.isResolved());
        assertNull(alert.getResolvedAt());
        assertFalse(alert.isCleared());
        assertNotNull(alert.getCreatedAt());
    }

    @Test
    void resolveRecordsTheTimeAndIsIdempotent() {
        var alert = create(ADMIN, AlertSource.SENSOR);

        var first = success(service.handle(new ResolveAlertCommand(alert.getId(), ADMIN)));
        var resolvedAt = first.getResolvedAt();
        var second = success(service.handle(new ResolveAlertCommand(alert.getId(), ADMIN)));

        assertTrue(second.isResolved());
        assertNotNull(resolvedAt);
        assertSame(resolvedAt, second.getResolvedAt());
    }

    @Test
    void resolveOfAnUnknownAlertIsNotFound() {
        var error = error(service.handle(new ResolveAlertCommand(404L, ADMIN)));

        assertEquals("ALERT_NOT_FOUND", error.code());
    }

    @Test
    void resolveOfAnotherAdminsAlertIsForbidden() {
        var alert = create(OTHER_ADMIN, AlertSource.SENSOR);

        var error = error(service.handle(new ResolveAlertCommand(alert.getId(), ADMIN)));

        assertEquals("FORBIDDEN", error.code());
        assertFalse(alert.isResolved());
    }

    @Test
    void resolveOfAClearedAlertIsNotFound() {
        var alert = createResolved(ADMIN);
        success(service.handle(new ClearAlertCommand(alert.getId(), ADMIN)));

        var error = error(service.handle(new ResolveAlertCommand(alert.getId(), ADMIN)));

        assertEquals("ALERT_NOT_FOUND", error.code());
    }

    @Test
    void clearOfAResolvedAlertMarksItCleared() {
        var alert = createResolved(ADMIN);

        success(service.handle(new ClearAlertCommand(alert.getId(), ADMIN)));

        assertTrue(repository.findById(alert.getId()).orElseThrow().isCleared());
    }

    @Test
    void clearIsIdempotent() {
        var alert = createResolved(ADMIN);
        var first = success(service.handle(new ClearAlertCommand(alert.getId(), ADMIN)));
        var clearedAt = first.getClearedAt();

        var second = success(service.handle(new ClearAlertCommand(alert.getId(), ADMIN)));

        assertSame(clearedAt, second.getClearedAt());
    }

    @Test
    void clearOfAPendingAlertIsABusinessRuleViolationWithPlainDetails() {
        var alert = create(ADMIN, AlertSource.MAINTENANCE);

        var error = error(service.handle(new ClearAlertCommand(alert.getId(), ADMIN)));

        assertEquals("BUSINESS_RULE_VIOLATION", error.code());
        assertEquals("Only resolved alerts can be cleared; resolve the alert first.", error.details());
        assertFalse(repository.findById(alert.getId()).orElseThrow().isCleared());
    }

    @Test
    void clearOfAnUnknownAlertIsNotFound() {
        assertEquals("ALERT_NOT_FOUND", error(service.handle(new ClearAlertCommand(404L, ADMIN))).code());
    }

    @Test
    void clearOfAnotherAdminsAlertIsForbidden() {
        var alert = createResolved(OTHER_ADMIN);

        var error = error(service.handle(new ClearAlertCommand(alert.getId(), ADMIN)));

        assertEquals("FORBIDDEN", error.code());
        assertFalse(alert.isCleared());
    }

    @Test
    void clearResolvedClearsOnlyTheCallersResolvedAlerts() {
        var resolvedA = createResolved(ADMIN);
        var resolvedB = createResolved(ADMIN);
        var pending = create(ADMIN, AlertSource.SENSOR);
        var othersResolved = createResolved(OTHER_ADMIN);

        var cleared = success(service.handle(new ClearResolvedAlertsCommand(ADMIN)));

        assertEquals(2, cleared);
        assertTrue(resolvedA.isCleared());
        assertTrue(resolvedB.isCleared());
        assertFalse(pending.isCleared());
        assertFalse(othersResolved.isCleared());
    }

    @Test
    void clearResolvedWithNothingToClearSucceeds() {
        create(ADMIN, AlertSource.SENSOR);

        assertEquals(0, success(service.handle(new ClearResolvedAlertsCommand(ADMIN))));
    }
}
