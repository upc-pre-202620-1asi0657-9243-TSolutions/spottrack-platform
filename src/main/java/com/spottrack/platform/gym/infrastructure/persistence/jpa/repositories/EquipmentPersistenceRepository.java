package com.spottrack.platform.gym.infrastructure.persistence.jpa.repositories;

import com.spottrack.platform.gym.domain.model.aggregates.Equipment;
import com.spottrack.platform.gym.domain.model.valueobjects.EquipmentId;
import com.spottrack.platform.gym.domain.model.valueobjects.EquipmentStatus;
import com.spottrack.platform.gym.infrastructure.persistence.jpa.entities.EquipmentPersistenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface EquipmentPersistenceRepository extends JpaRepository<EquipmentPersistenceEntity, Long> {
    Optional<EquipmentPersistenceEntity> findByEquipmentId(String equipmentId);
    Optional<EquipmentPersistenceEntity> findByEquipmentName(String equipmentName);
    Optional<EquipmentPersistenceEntity> findByStatus(EquipmentStatus equipmentStatus);
    List<EquipmentPersistenceEntity> findByEquipmentNameAndStatusAndEquipmentIdNot(String equipmentName, EquipmentStatus status, String equipmentId);
    List<EquipmentPersistenceEntity> findByMaintenanceThresholdLessThanEqualAndMaintenanceAlertSentFalse(LocalDate date);
    List<EquipmentPersistenceEntity> findByZoneId(String zoneId);
}
