package com.spottrack.platform.gym.interfaces.rest.resources;

import com.spottrack.platform.gym.domain.model.valueobjects.EquipmentStatus;
import com.spottrack.platform.gym.domain.model.valueobjects.ManufacturerId;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;

public record EquipmentResource(
        String equipmentId,
        String equipmentName,
        EquipmentStatus status,
        String model,
        String manufacturerId,
        String zoneId,
        String purchaseCurrency,
        BigDecimal purchaseAmount,
        LocalDate maintenanceThreshold
) {
}
