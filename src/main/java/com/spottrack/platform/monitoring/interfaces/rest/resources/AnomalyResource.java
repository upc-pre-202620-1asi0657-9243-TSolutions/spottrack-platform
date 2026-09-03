package com.spottrack.platform.monitoring.interfaces.rest.resources;


import com.spottrack.platform.monitoring.domain.model.valueobjects.AnomalyId;
import com.spottrack.platform.monitoring.domain.model.valueobjects.EquipmentId;
import com.spottrack.platform.monitoring.domain.model.valueobjects.ZoneId;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

public record AnomalyResource(
        @Schema
        Long id,
        @Schema
        String anomalyId,
        @Schema
         String reservationId,
        @Schema
         String equipmentId,
        @Schema
         String zoneId,
        @Schema
         String anomalyDescription,
        @Schema
         LocalDate emissionDate
) {
}
