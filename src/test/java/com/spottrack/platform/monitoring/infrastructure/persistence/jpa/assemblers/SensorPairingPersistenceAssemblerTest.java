package com.spottrack.platform.monitoring.infrastructure.persistence.jpa.assemblers;

import com.spottrack.platform.monitoring.domain.model.aggregates.SensorPairing;
import com.spottrack.platform.monitoring.domain.model.valueobjects.MotionSensorId;
import com.spottrack.platform.monitoring.domain.model.valueobjects.SensorPairingStatus;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SensorPairingPersistenceAssemblerTest {

    @Test
    void roundTripKeepsEveryField() {
        var issuedAt = LocalDateTime.of(2026, 11, 15, 12, 0);
        var domain = new SensorPairing(7L, new MotionSensorId("dev-1"), "hash", issuedAt, issuedAt.plusMinutes(15),
                SensorPairingStatus.USED, issuedAt.plusMinutes(2));

        var entity = SensorPairingPersistenceAssembler.toPersistenceFromDomain(domain);
        var restored = SensorPairingPersistenceAssembler.toDomainFromPersistence(entity);

        assertEquals(7L, entity.getId());
        assertEquals(new MotionSensorId("dev-1"), restored.getMotionSensorId());
        assertEquals("hash", restored.getSecretHash());
        assertEquals(issuedAt, restored.getIssuedAt());
        assertEquals(issuedAt.plusMinutes(15), restored.getExpiresAt());
        assertEquals(SensorPairingStatus.USED, restored.getStatus());
        assertEquals(issuedAt.plusMinutes(2), restored.getUsedAt());
    }
}
