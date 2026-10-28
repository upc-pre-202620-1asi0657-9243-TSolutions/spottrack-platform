package com.spottrack.platform.shared.application.internal.queryservices;

import com.spottrack.platform.shared.application.internal.commandservices.InMemoryAlertRepository;
import com.spottrack.platform.shared.domain.model.aggregates.Alert;
import com.spottrack.platform.shared.domain.model.queries.GetAlertsByAdminUserIdQuery;
import com.spottrack.platform.shared.domain.model.valueobjects.AlertSeverity;
import com.spottrack.platform.shared.domain.model.valueobjects.AlertSource;
import com.spottrack.platform.shared.domain.model.valueobjects.AlertStatusFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AlertQueryServiceImplTest {

    private static final Long ADMIN = 42L;
    private static final Date NOW = new Date(5_000L);

    private InMemoryAlertRepository repository;
    private AlertQueryServiceImpl service;
    private Alert pendingMaintenance;
    private Alert pendingAnomaly;
    private Alert resolvedSensor;
    private Alert clearedSensor;
    private Alert legacyWithoutSource;

    @BeforeEach
    void setUp() {
        repository = new InMemoryAlertRepository();
        service = new AlertQueryServiceImpl(repository);
        pendingMaintenance = save(ADMIN, AlertSeverity.WARNING, AlertSource.MAINTENANCE);
        pendingAnomaly = save(ADMIN, AlertSeverity.CRITICAL, AlertSource.ANOMALY);
        resolvedSensor = save(ADMIN, AlertSeverity.WARNING, AlertSource.SENSOR);
        resolvedSensor.resolve(NOW);
        clearedSensor = save(ADMIN, AlertSeverity.WARNING, AlertSource.SENSOR);
        clearedSensor.resolve(NOW);
        clearedSensor.clear(NOW);
        legacyWithoutSource = save(ADMIN, AlertSeverity.CRITICAL, null);
        save(99L, AlertSeverity.CRITICAL, AlertSource.ANOMALY);
    }

    private Alert save(Long adminId, AlertSeverity severity, AlertSource source) {
        return repository.save(new Alert(null, adminId, "eq-1", severity, source, "msg", false, null, null, NOW));
    }

    private List<Alert> query(AlertStatusFilter status, AlertSource source, AlertSeverity severity) {
        return service.handle(new GetAlertsByAdminUserIdQuery(ADMIN, status, source, severity));
    }

    @Test
    void defaultsToPendingAlertsOnly() {
        assertEquals(List.of(pendingMaintenance, pendingAnomaly, legacyWithoutSource), query(null, null, null));
    }

    @Test
    void resolvedStatusReturnsOnlyResolvedAlertsAndNeverTheCleared() {
        assertEquals(List.of(resolvedSensor), query(AlertStatusFilter.RESOLVED, null, null));
    }

    @Test
    void allStatusReturnsPendingAndResolvedButNeverTheCleared() {
        assertEquals(List.of(pendingMaintenance, pendingAnomaly, resolvedSensor, legacyWithoutSource),
                query(AlertStatusFilter.ALL, null, null));
    }

    @Test
    void filtersBySource() {
        assertEquals(List.of(pendingAnomaly), query(AlertStatusFilter.ALL, AlertSource.ANOMALY, null));
    }

    @Test
    void filtersBySeverity() {
        assertEquals(List.of(pendingAnomaly, legacyWithoutSource),
                query(AlertStatusFilter.PENDING, null, AlertSeverity.CRITICAL));
    }

    @Test
    void combinesTheFilters() {
        assertEquals(List.of(resolvedSensor),
                query(AlertStatusFilter.ALL, AlertSource.SENSOR, AlertSeverity.WARNING));
    }

    @Test
    void neverReturnsAnotherAdminsAlerts() {
        var others = service.handle(new GetAlertsByAdminUserIdQuery(7L, AlertStatusFilter.ALL, null, null));

        assertEquals(List.of(), others);
    }
}
