package com.spottrack.platform.monitoring.testsupport;

import com.spottrack.platform.monitoring.domain.model.aggregates.SensorPairing;
import com.spottrack.platform.monitoring.domain.model.valueobjects.MotionSensorId;
import com.spottrack.platform.monitoring.domain.model.valueobjects.SensorPairingStatus;
import com.spottrack.platform.monitoring.domain.repositories.SensorPairingRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** Mirrors the queries of the JPA adapter: the same status filters, over a list. */
public class InMemorySensorPairingRepository implements SensorPairingRepository {
    private final List<SensorPairing> stored = new ArrayList<>();

    @Override
    public SensorPairing save(SensorPairing sensorPairing) {
        if (!stored.contains(sensorPairing)) stored.add(sensorPairing);
        return sensorPairing;
    }

    @Override
    public List<SensorPairing> findPendingByMotionSensorId(MotionSensorId motionSensorId) {
        return stored.stream()
                .filter(p -> p.getMotionSensorId().equals(motionSensorId) && p.getStatus() == SensorPairingStatus.PENDING)
                .toList();
    }

    @Override
    public List<SensorPairing> findActiveByMotionSensorId(MotionSensorId motionSensorId) {
        return stored.stream()
                .filter(p -> p.getMotionSensorId().equals(motionSensorId)
                        && (p.getStatus() == SensorPairingStatus.PENDING || p.getStatus() == SensorPairingStatus.USED))
                .toList();
    }

    @Override
    public List<SensorPairing> findPendingExpiredBefore(LocalDateTime now) {
        return stored.stream()
                .filter(p -> p.getStatus() == SensorPairingStatus.PENDING && p.getExpiresAt().isBefore(now))
                .toList();
    }

    public List<SensorPairing> all() {
        return List.copyOf(stored);
    }
}
