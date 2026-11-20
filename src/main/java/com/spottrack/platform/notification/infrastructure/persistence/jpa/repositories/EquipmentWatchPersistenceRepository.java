package com.spottrack.platform.notification.infrastructure.persistence.jpa.repositories;

import com.spottrack.platform.notification.domain.model.valueobjects.WatchOrigin;
import com.spottrack.platform.notification.domain.model.valueobjects.WatchStatus;
import com.spottrack.platform.notification.infrastructure.persistence.jpa.entities.EquipmentWatchPersistenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EquipmentWatchPersistenceRepository extends JpaRepository<EquipmentWatchPersistenceEntity, Long> {
    Optional<EquipmentWatchPersistenceEntity> findFirstByClientIdAndEquipmentIdAndOriginAndStatus(
            Long clientId, String equipmentId, WatchOrigin origin, WatchStatus status);

    List<EquipmentWatchPersistenceEntity> findAllByClientIdAndStatus(Long clientId, WatchStatus status);

    List<EquipmentWatchPersistenceEntity> findAllByEquipmentIdAndStatus(String equipmentId, WatchStatus status);
}
