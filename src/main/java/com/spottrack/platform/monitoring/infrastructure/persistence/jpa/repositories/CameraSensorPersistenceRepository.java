package com.spottrack.platform.monitoring.infrastructure.persistence.jpa.repositories;

import com.spottrack.platform.monitoring.domain.model.valueobjects.EquipmentId;
import com.spottrack.platform.monitoring.infrastructure.persistence.jpa.entities.CameraSensorPersistenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CameraSensorPersistenceRepository extends JpaRepository<CameraSensorPersistenceEntity, Long> {
    boolean existsByEquipmentId(EquipmentId equipmentId);
}
