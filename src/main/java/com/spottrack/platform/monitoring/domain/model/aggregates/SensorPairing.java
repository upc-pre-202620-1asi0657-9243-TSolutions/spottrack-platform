package com.spottrack.platform.monitoring.domain.model.aggregates;

import com.spottrack.platform.monitoring.domain.model.valueobjects.MotionSensorId;
import com.spottrack.platform.monitoring.domain.model.valueobjects.SensorPairingStatus;
import com.spottrack.platform.shared.domain.model.aggregates.AbstractDomainAggregateRoot;
import lombok.Getter;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * A QR pairing issued for a motion sensor. Only the hash of the secret is kept.
 * <p>
 * The TTL only bounds how long the QR can be used before it is scanned: once the
 * first heartbeat arrives the pairing is {@code USED} and its credential stays.
 */
@Getter
public class SensorPairing extends AbstractDomainAggregateRoot<SensorPairing> {
    private final Long id;
    private final MotionSensorId motionSensorId;
    private final String secretHash;
    private final LocalDateTime issuedAt;
    private final LocalDateTime expiresAt;
    private SensorPairingStatus status;
    private LocalDateTime usedAt;

    public SensorPairing(Long id, MotionSensorId motionSensorId, String secretHash, LocalDateTime issuedAt,
                         LocalDateTime expiresAt, SensorPairingStatus status, LocalDateTime usedAt) {
        this.id = id;
        this.motionSensorId = motionSensorId;
        this.secretHash = secretHash;
        this.issuedAt = issuedAt;
        this.expiresAt = expiresAt;
        this.status = status;
        this.usedAt = usedAt;
    }

    public static SensorPairing issue(MotionSensorId motionSensorId, String secretHash, LocalDateTime now, Duration ttl) {
        if (motionSensorId == null) {
            throw new IllegalArgumentException("sensorPairing.error.motionSensorId.required");
        }
        if (secretHash == null || secretHash.isBlank()) {
            throw new IllegalArgumentException("sensorPairing.error.secretHash.notBlank");
        }
        if (ttl == null || ttl.isZero() || ttl.isNegative()) {
            throw new IllegalArgumentException("sensorPairing.error.ttl.positive");
        }
        return new SensorPairing(null, motionSensorId, secretHash, now, now.plus(ttl),
                SensorPairingStatus.PENDING, null);
    }

    public boolean isPending() {
        return status == SensorPairingStatus.PENDING;
    }

    public boolean isExpiredAt(LocalDateTime now) {
        return now.isAfter(expiresAt);
    }

    /** First sign of life of the sensor: only a pending, unexpired pairing can be used. */
    public void markUsed(LocalDateTime now) {
        if (!isPending()) {
            throw new IllegalStateException("Sensor pairing is " + status + ", not PENDING");
        }
        if (isExpiredAt(now)) {
            throw new IllegalStateException("Sensor pairing expired at " + expiresAt);
        }
        this.status = SensorPairingStatus.USED;
        this.usedAt = now;
    }

    public void expire(LocalDateTime now) {
        if (!isPending()) {
            throw new IllegalStateException("Sensor pairing is " + status + ", not PENDING");
        }
        if (!isExpiredAt(now)) {
            throw new IllegalStateException("Sensor pairing has not expired yet (expires at " + expiresAt + ")");
        }
        this.status = SensorPairingStatus.EXPIRED;
    }

    /** Replaced by a newer pairing of the same sensor. */
    public void revoke() {
        if (status != SensorPairingStatus.PENDING && status != SensorPairingStatus.USED) {
            throw new IllegalStateException("Sensor pairing is " + status + ", it cannot be revoked");
        }
        this.status = SensorPairingStatus.REVOKED;
    }
}
