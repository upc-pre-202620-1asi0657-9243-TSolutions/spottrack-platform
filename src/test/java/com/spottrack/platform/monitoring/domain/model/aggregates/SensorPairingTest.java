package com.spottrack.platform.monitoring.domain.model.aggregates;

import com.spottrack.platform.monitoring.domain.model.valueobjects.MotionSensorId;
import com.spottrack.platform.monitoring.domain.model.valueobjects.SensorPairingStatus;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SensorPairingTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 11, 15, 12, 0);
    private static final Duration TTL = Duration.ofMinutes(15);
    private static final MotionSensorId SENSOR = new MotionSensorId("dev-1");

    private static SensorPairing pairing() {
        return SensorPairing.issue(SENSOR, "hash", NOW, TTL);
    }

    @Test
    void issuedPairingIsPendingAndExpiresAfterTheTtl() {
        var pairing = pairing();

        assertEquals(SensorPairingStatus.PENDING, pairing.getStatus());
        assertEquals(NOW, pairing.getIssuedAt());
        assertEquals(NOW.plus(TTL), pairing.getExpiresAt());
        assertNull(pairing.getUsedAt());
        assertNull(pairing.getId());
    }

    @Test
    void issueRejectsInvalidArguments() {
        assertThrows(IllegalArgumentException.class, () -> SensorPairing.issue(null, "hash", NOW, TTL));
        assertThrows(IllegalArgumentException.class, () -> SensorPairing.issue(SENSOR, " ", NOW, TTL));
        assertThrows(IllegalArgumentException.class, () -> SensorPairing.issue(SENSOR, null, NOW, TTL));
        assertThrows(IllegalArgumentException.class, () -> SensorPairing.issue(SENSOR, "hash", NOW, Duration.ZERO));
        assertThrows(IllegalArgumentException.class, () -> SensorPairing.issue(SENSOR, "hash", NOW, Duration.ofSeconds(-1)));
    }

    @Test
    void isExpiredOnlyAfterTheExpirationInstant() {
        var pairing = pairing();

        assertFalse(pairing.isExpiredAt(NOW.plus(TTL)));
        assertTrue(pairing.isExpiredAt(NOW.plus(TTL).plusSeconds(1)));
    }

    @Test
    void firstHeartbeatMarksItUsedWithTheTimestamp() {
        var pairing = pairing();
        var seenAt = NOW.plusMinutes(3);

        pairing.markUsed(seenAt);

        assertEquals(SensorPairingStatus.USED, pairing.getStatus());
        assertEquals(seenAt, pairing.getUsedAt());
    }

    @Test
    void anExpiredPairingCannotBeUsed() {
        var pairing = pairing();

        assertThrows(IllegalStateException.class, () -> pairing.markUsed(NOW.plus(TTL).plusSeconds(1)));
        assertEquals(SensorPairingStatus.PENDING, pairing.getStatus());
        assertNull(pairing.getUsedAt());
    }

    @Test
    void aUsedPairingCannotBeUsedAgain() {
        var pairing = pairing();
        pairing.markUsed(NOW.plusMinutes(1));

        assertThrows(IllegalStateException.class, () -> pairing.markUsed(NOW.plusMinutes(2)));
        assertEquals(NOW.plusMinutes(1), pairing.getUsedAt());
    }

    @Test
    void aDuePairingExpires() {
        var pairing = pairing();

        pairing.expire(NOW.plus(TTL).plusSeconds(1));

        assertEquals(SensorPairingStatus.EXPIRED, pairing.getStatus());
    }

    @Test
    void aPairingThatIsNotDueCannotExpire() {
        var pairing = pairing();

        assertThrows(IllegalStateException.class, () -> pairing.expire(NOW.plusMinutes(1)));
        assertEquals(SensorPairingStatus.PENDING, pairing.getStatus());
    }

    @Test
    void aUsedPairingNeverExpires() {
        var pairing = pairing();
        pairing.markUsed(NOW.plusMinutes(1));

        assertThrows(IllegalStateException.class, () -> pairing.expire(NOW.plusDays(1)));
        assertEquals(SensorPairingStatus.USED, pairing.getStatus());
    }

    @Test
    void pendingAndUsedPairingsCanBeRevoked() {
        var pending = pairing();
        var used = pairing();
        used.markUsed(NOW.plusMinutes(1));

        pending.revoke();
        used.revoke();

        assertEquals(SensorPairingStatus.REVOKED, pending.getStatus());
        assertEquals(SensorPairingStatus.REVOKED, used.getStatus());
    }

    @Test
    void expiredAndRevokedPairingsCannotBeRevoked() {
        var expired = pairing();
        expired.expire(NOW.plusDays(1));
        var revoked = pairing();
        revoked.revoke();

        assertThrows(IllegalStateException.class, expired::revoke);
        assertThrows(IllegalStateException.class, revoked::revoke);
    }
}
