package com.spottrack.platform.gym.domain.model.commands;

import com.spottrack.platform.gym.domain.model.valueobjects.EquipmentStatus;
import com.spottrack.platform.gym.domain.model.valueobjects.ManufacturerId;
import com.spottrack.platform.gym.domain.model.valueobjects.ZoneId;
import com.spottrack.platform.shared.domain.model.valueobjects.Money;

import java.time.LocalDate;

public record RegisterEquipment(
        String equipmentName,
        EquipmentStatus status,
        String model,
        ManufacturerId manufacturerId,
        ZoneId zoneId,
        Money purchasePrice,
        LocalDate maintenanceThreshold
) {
    public RegisterEquipment {
        if (equipmentName == null || equipmentName.isBlank()) {
            throw new IllegalArgumentException("equipment.command.registerEquipment.equipmentName.notBlank");
        }
        if (model == null || model.isBlank()) {
            throw new IllegalArgumentException("equipment.command.registerEquipment.model.notBlank");
        }
    }
}
