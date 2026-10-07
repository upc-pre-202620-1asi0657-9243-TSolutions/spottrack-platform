package com.spottrack.platform.monitoring.infrastructure.persistence.jpa.repositories;

import com.spottrack.platform.monitoring.domain.model.valueobjects.EquipmentId;
import com.spottrack.platform.monitoring.domain.model.valueobjects.MotionSensorId;
import com.spottrack.platform.monitoring.infrastructure.persistence.jpa.entities.MotionSensorPersistenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface MotionSensorPersistenceRepository extends JpaRepository<MotionSensorPersistenceEntity, Long> {
    boolean existsByEquipmentId(EquipmentId equipmentId);
    Optional<MotionSensorPersistenceEntity> findByMotionSensorId(MotionSensorId motionSensorId);
    List<MotionSensorPersistenceEntity> findByOnlineTrue();
    List<MotionSensorPersistenceEntity> findByOnlineFalseAndLastStatusChangeAtBefore(LocalDateTime threshold);
}
