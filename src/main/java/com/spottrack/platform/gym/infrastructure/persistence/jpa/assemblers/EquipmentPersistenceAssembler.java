package com.spottrack.platform.gym.infrastructure.persistence.jpa.assemblers;

import com.spottrack.platform.gym.domain.model.aggregates.Equipment;
import com.spottrack.platform.gym.domain.model.valueobjects.EquipmentId;
import com.spottrack.platform.gym.domain.model.valueobjects.ManufacturerId;
import com.spottrack.platform.gym.domain.model.valueobjects.ZoneId;
import com.spottrack.platform.gym.infrastructure.persistence.jpa.entities.EquipmentPersistenceEntity;

public class EquipmentPersistenceAssembler {
    private  EquipmentPersistenceAssembler(){

    }

    public static Equipment toDomainFromPersistence(EquipmentPersistenceEntity entity){
        var equipment = new Equipment();
        equipment.setPersistenceId(entity.getId());
        equipment.setId(new EquipmentId(entity.getEquipmentId()));
        equipment.setEquipmentName(entity.getEquipmentName());
        equipment.setModel(entity.getModel());
        equipment.setStatus(entity.getStatus());
        equipment.setManufacturerId(entity.getManufacturerId());
        equipment.setZoneId(entity.getZoneId() != null && !entity.getZoneId().isBlank() ? new ZoneId(entity.getZoneId()) : null);
        equipment.setPurchasePrice(entity.getPurchasePrice());
        equipment.setMaintenanceThreshold(entity.getMaintenanceThreshold());
        equipment.setMaintenanceAlertSent(entity.isMaintenanceAlertSent());
        return equipment;
    }

    public static EquipmentPersistenceEntity toPersistenceFromDomain(Equipment domain){
        var equipment = new EquipmentPersistenceEntity();
        equipment.setId(domain.getPersistenceId());
        equipment.setEquipmentId(domain.getId().uuid());
        equipment.setEquipmentName(domain.getEquipmentName());
        equipment.setStatus(domain.getStatus());
        equipment.setModel(domain.getModel());
        equipment.setManufacturerId(domain.getManufacturerId());
        equipment.setZoneId(domain.getZoneId() != null ? domain.getZoneId().uuid() : null);
        equipment.setPurchasePrice(domain.getPurchasePrice());
        equipment.setMaintenanceThreshold(domain.getMaintenanceThreshold());
        equipment.setMaintenanceAlertSent(domain.isMaintenanceAlertSent());
        return equipment;
    }

}
