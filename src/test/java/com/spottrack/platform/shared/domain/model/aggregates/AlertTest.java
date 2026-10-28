package com.spottrack.platform.shared.domain.model.aggregates;

import com.spottrack.platform.shared.domain.model.valueobjects.AlertSeverity;
import com.spottrack.platform.shared.domain.model.valueobjects.AlertSource;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AlertTest {

    private static final Date CREATED = new Date(1_000L);
    private static final Date FIRST = new Date(2_000L);
    private static final Date SECOND = new Date(3_000L);

    private static Alert pending() {
        return new Alert(1L, 42L, "eq-1", AlertSeverity.WARNING, AlertSource.SENSOR, "msg", false, null, null, CREATED);
    }

    @Test
    void newAlertIsPendingAndVisible() {
        var alert = pending();

        assertFalse(alert.isResolved());
        assertNull(alert.getResolvedAt());
        assertFalse(alert.isCleared());
    }

    @Test
    void resolvingRecordsTheResolutionTime() {
        var alert = pending();

        alert.resolve(FIRST);

        assertTrue(alert.isResolved());
        assertSame(FIRST, alert.getResolvedAt());
    }

    @Test
    void resolvingAnAlreadyResolvedAlertKeepsTheOriginalTime() {
        var alert = pending();
        alert.resolve(FIRST);

        alert.resolve(SECOND);

        assertTrue(alert.isResolved());
        assertSame(FIRST, alert.getResolvedAt());
    }

    @Test
    void aLegacyResolvedAlertWithoutTimestampStaysResolved() {
        var legacy = new Alert(1L, 42L, "eq-1", AlertSeverity.CRITICAL, null, "msg", true, null, null, CREATED);

        legacy.resolve(FIRST);

        assertTrue(legacy.isResolved());
        assertNull(legacy.getResolvedAt());
        assertNull(legacy.getSource());
    }

    @Test
    void aPendingAlertCannotBeCleared() {
        var alert = pending();

        var error = assertThrows(IllegalStateException.class, () -> alert.clear(FIRST));

        assertEquals("Only resolved alerts can be cleared; resolve the alert first.", error.getMessage());
        assertFalse(alert.isCleared());
    }

    @Test
    void aResolvedAlertCanBeCleared() {
        var alert = pending();
        alert.resolve(FIRST);

        alert.clear(SECOND);

        assertTrue(alert.isCleared());
        assertSame(SECOND, alert.getClearedAt());
    }

    @Test
    void clearingAnAlreadyClearedAlertKeepsTheOriginalTime() {
        var alert = pending();
        alert.resolve(FIRST);
        alert.clear(FIRST);

        alert.clear(SECOND);

        assertSame(FIRST, alert.getClearedAt());
    }

    @Test
    void requiresTheMandatoryFields() {
        assertThrows(NullPointerException.class,
                () -> new Alert(1L, null, "eq-1", AlertSeverity.WARNING, null, "msg", false, null, null, CREATED));
        assertThrows(NullPointerException.class,
                () -> new Alert(1L, 42L, "eq-1", null, null, "msg", false, null, null, CREATED));
    }
}
