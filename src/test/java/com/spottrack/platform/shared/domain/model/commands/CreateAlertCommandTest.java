package com.spottrack.platform.shared.domain.model.commands;

import com.spottrack.platform.shared.domain.model.valueobjects.AlertSeverity;
import com.spottrack.platform.shared.domain.model.valueobjects.AlertSource;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CreateAlertCommandTest {

    @Test
    void requiresTheSourceOfTheAlert() {
        var error = assertThrows(IllegalArgumentException.class,
                () -> new CreateAlertCommand(42L, "eq-1", AlertSeverity.WARNING, null, "msg"));

        assertEquals("Source must not be null", error.getMessage());
    }

    @Test
    void acceptsAValidCommand() {
        var command = new CreateAlertCommand(42L, "eq-1", AlertSeverity.CRITICAL, AlertSource.ANOMALY, "msg");

        assertEquals(AlertSource.ANOMALY, command.source());
    }
}
