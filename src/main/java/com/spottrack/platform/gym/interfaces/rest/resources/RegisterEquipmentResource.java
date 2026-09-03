package com.spottrack.platform.gym.interfaces.rest.resources;

import com.spottrack.platform.gym.domain.model.aggregates.Equipment;
import com.spottrack.platform.gym.domain.model.valueobjects.EquipmentStatus;
import com.spottrack.platform.gym.domain.model.valueobjects.ManufacturerId;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;

public record RegisterEquipmentResource(
        @Schema
        String equipmentName,
        @Schema
        EquipmentStatus status,
        @Schema
        String model,
        @Schema
        String manufacturerId,
        @Schema
        String zoneId,
        @Schema
        String purchaseCurrency,
        @Schema
        BigDecimal purchaseAmount,
        @Schema(description = "Optional maintenance threshold date; defaults to today if not provided")
        LocalDate maintenanceThreshold
) {
}
