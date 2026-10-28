package com.spottrack.platform.shared.interfaces.rest.transform;

import com.spottrack.platform.shared.domain.model.aggregates.Alert;
import com.spottrack.platform.shared.domain.model.valueobjects.AlertSeverity;
import com.spottrack.platform.shared.domain.model.valueobjects.AlertSource;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AlertResourceFromEntityAssemblerTest {

    private static final Date CREATED = new Date(1_000L);
    private static final Date RESOLVED = new Date(2_000L);

    @Test
    void exposesSourceAndResolvedAtAlongsideTheExistingFields() {
        var alert = new Alert(5L, 42L, "eq-1", AlertSeverity.WARNING, AlertSource.SENSOR, "msg",
                true, RESOLVED, null, CREATED);

        var resource = AlertResourceFromEntityAssembler.toResourceFromEntity(alert);

        assertEquals(5L, resource.id());
        assertEquals("eq-1", resource.equipmentId());
        assertEquals("WARNING", resource.severity());
        assertEquals("msg", resource.message());
        assertTrue(resource.isResolved());
        assertEquals(CREATED, resource.createdAt());
        assertEquals("SENSOR", resource.source());
        assertEquals(RESOLVED, resource.resolvedAt());
    }

    @Test
    void legacyAlertsExposeNullSourceAndResolvedAt() {
        var legacy = new Alert(5L, 42L, "eq-1", AlertSeverity.CRITICAL, null, "msg", false, null, null, CREATED);

        var resource = AlertResourceFromEntityAssembler.toResourceFromEntity(legacy);

        assertNull(resource.source());
        assertNull(resource.resolvedAt());
        assertFalse(resource.isResolved());
    }
}
