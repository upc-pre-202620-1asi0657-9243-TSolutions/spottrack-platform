package com.spottrack.platform.maintenance.infrastructure.persistence.jpa.assemblers;

import com.spottrack.platform.maintenance.domain.model.aggregates.Technician;
import com.spottrack.platform.maintenance.infrastructure.persistence.jpa.entities.TechnicianPersistenceEntity;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class TechnicianPersistenceAssemblerTest {

    @Test
    void roundTripKeepsUserAndAdminLinks() {
        var technician = new Technician("tech-1", "Maria Perez", 10L, 42L);

        var entity = TechnicianPersistenceAssembler.toPersistenceFromDomain(technician);
        var restored = TechnicianPersistenceAssembler.toDomainFromPersistence(entity);

        assertEquals("tech-1", entity.getTechnicianId());
        assertEquals(10L, entity.getUserId());
        assertEquals(42L, entity.getAdminUserId());
        assertEquals("tech-1", restored.getTechnicianId().uuid());
        assertEquals("Maria Perez", restored.getName());
        assertEquals(10L, restored.getUserId());
        assertEquals(42L, restored.getAdminUserId());
    }

    @Test
    void legacyRowsWithoutLinksStayNull() {
        var entity = new TechnicianPersistenceEntity();
        entity.setTechnicianId("tech-legacy");
        entity.setName("Carlos Seed");

        var domain = TechnicianPersistenceAssembler.toDomainFromPersistence(entity);

        assertNull(domain.getUserId());
        assertNull(domain.getAdminUserId());
    }
}
