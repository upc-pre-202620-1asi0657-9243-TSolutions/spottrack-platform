package com.spottrack.platform.shared.infrastructure.persistence.jpa.assemblers;

import com.spottrack.platform.shared.domain.model.aggregates.Alert;
import com.spottrack.platform.shared.domain.model.valueobjects.AlertSeverity;
import com.spottrack.platform.shared.domain.model.valueobjects.AlertSource;
import com.spottrack.platform.shared.infrastructure.persistence.jpa.entities.AlertPersistenceEntity;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AlertPersistenceAssemblerTest {

    private static final Date CREATED = new Date(1_000L);
    private static final Date RESOLVED = new Date(2_000L);
    private static final Date CLEARED = new Date(3_000L);

    @Test
    void roundTripsSourceResolutionAndClearingTimes() {
        var alert = new Alert(5L, 42L, "eq-1", AlertSeverity.CRITICAL, AlertSource.ANOMALY, "msg",
                true, RESOLVED, CLEARED, CREATED);

        var entity = AlertPersistenceAssembler.toPersistenceFromDomain(alert);
        var restored = AlertPersistenceAssembler.toDomainFromPersistence(entity);

        assertEquals(AlertSource.ANOMALY, entity.getSource());
        assertEquals(RESOLVED, entity.getResolvedAt());
        assertEquals(CLEARED, entity.getClearedAt());
        assertEquals(AlertSource.ANOMALY, restored.getSource());
        assertTrue(restored.isResolved());
        assertEquals(RESOLVED, restored.getResolvedAt());
        assertTrue(restored.isCleared());
        assertEquals(CLEARED, restored.getClearedAt());
    }

    @Test
    void rowsCreatedBeforeTheNewColumnsMapToNullSourceAndTimes() {
        var legacy = new AlertPersistenceEntity();
        legacy.setId(9L);
        legacy.setCreatedAt(CREATED);
        legacy.setAdminUserId(42L);
        legacy.setEquipmentId("eq-1");
        legacy.setSeverity(AlertSeverity.WARNING);
        legacy.setMessage("old alert");
        legacy.setResolved(false);

        var alert = AlertPersistenceAssembler.toDomainFromPersistence(legacy);

        assertNull(alert.getSource());
        assertNull(alert.getResolvedAt());
        assertFalse(alert.isCleared());
    }
}
