package com.spottrack.platform.monitoring.domain.repositories;

import com.spottrack.platform.monitoring.domain.model.aggregates.SensorPairing;
import com.spottrack.platform.monitoring.domain.model.valueobjects.MotionSensorId;

import java.time.LocalDateTime;
import java.util.List;

public interface SensorPairingRepository {
    SensorPairing save(SensorPairing sensorPairing);

    List<SensorPairing> findPendingByMotionSensorId(MotionSensorId motionSensorId);

    /** Pairings whose secret is still the live credential of the sensor: PENDING or USED. */
    List<SensorPairing> findActiveByMotionSensorId(MotionSensorId motionSensorId);

    List<SensorPairing> findPendingExpiredBefore(LocalDateTime now);
}
