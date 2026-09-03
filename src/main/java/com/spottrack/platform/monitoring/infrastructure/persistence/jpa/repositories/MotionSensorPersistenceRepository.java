package com.spottrack.platform.monitoring.infrastructure.persistence.jpa.repositories;

import com.spottrack.platform.monitoring.domain.model.valueobjects.EquipmentId;
import com.spottrack.platform.monitoring.infrastructure.persistence.jpa.entities.MotionSensorPersistenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MotionSensorPersistenceRepository extends JpaRepository<MotionSensorPersistenceEntity, Long> {
    boolean existsByEquipmentId(EquipmentId equipmentId);
    List<MotionSensorPersistenceEntity> findByOnlineTrue();
    List<MotionSensorPersistenceEntity> findByOnlineFalseAndLastStatusChangeAtBefore(LocalDateTime threshold);
}
