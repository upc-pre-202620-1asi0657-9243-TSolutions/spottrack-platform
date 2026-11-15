package com.spottrack.platform.monitoring.infrastructure.persistence.jpa.repositories;

import com.spottrack.platform.monitoring.domain.model.valueobjects.MotionSensorId;
import com.spottrack.platform.monitoring.domain.model.valueobjects.SensorPairingStatus;
import com.spottrack.platform.monitoring.infrastructure.persistence.jpa.entities.SensorPairingPersistenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Repository
public interface SensorPairingPersistenceRepository extends JpaRepository<SensorPairingPersistenceEntity, Long> {
    List<SensorPairingPersistenceEntity> findByMotionSensorIdAndStatusIn(MotionSensorId motionSensorId,
                                                                         Collection<SensorPairingStatus> statuses);

    List<SensorPairingPersistenceEntity> findByStatusAndExpiresAtBefore(SensorPairingStatus status, LocalDateTime threshold);
}
