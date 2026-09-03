package com.spottrack.platform.gym.domain.repositories;

import com.spottrack.platform.gym.domain.model.aggregates.Equipment;
import com.spottrack.platform.gym.domain.model.valueobjects.EquipmentId;
import com.spottrack.platform.gym.domain.model.valueobjects.EquipmentStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Definition for the equipment repository in the domain layer
 */
public interface EquipmentRepository {
    Optional<Equipment> findById(EquipmentId equipmentId);
    List<Equipment> findByStatus(EquipmentStatus status);
    List<Equipment> findByName(String equipmentName);
    List<Equipment> findAll();
    List<Equipment> findByMaintenanceThresholdLessThanEqualAndMaintenanceAlertSentFalse(LocalDate date);
    Equipment save(Equipment equipment);

}
