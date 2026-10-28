package com.spottrack.platform.maintenance.domain.model.commands;

import com.spottrack.platform.maintenance.domain.model.aggregates.Technician;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CreateTechnicianTest {

    @Test
    void seederConstructorLeavesTechnicianAsLegacyWithoutUserOrAdmin() {
        var command = new CreateTechnician("Carlos Seed");

        assertEquals("Carlos Seed", command.name());
        assertNull(command.userId());
        assertNull(command.adminUserId());
    }

    @Test
    void blankNameIsRejectedWithReadableMessage() {
        var error = assertThrows(IllegalArgumentException.class, () -> new CreateTechnician("  ", 1L, 2L));

        assertEquals("Technician name must not be blank", error.getMessage());
    }

    @Test
    void aggregateKeepsUserAndAdminFromTheCommand() {
        var technician = new Technician(new CreateTechnician("Maria Perez", 10L, 42L));

        assertNotNull(technician.getTechnicianId());
        assertEquals(10L, technician.getUserId());
        assertEquals(42L, technician.getAdminUserId());
    }
}
