package com.spottrack.platform.monitoring.domain.repositories;

import com.spottrack.platform.monitoring.domain.model.aggregates.MotionSensor;
import com.spottrack.platform.monitoring.domain.model.valueobjects.EquipmentId;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface MotionSensorRepository {
    boolean existsByEquipmentId(EquipmentId equipmentId);
    List<MotionSensor> findAll();
    Optional<MotionSensor> findById(Long id);
    List<MotionSensor> findAllOnline();
    List<MotionSensor> findAllOfflineSince(LocalDateTime threshold);
    MotionSensor save(MotionSensor motionSensor);
}
