package com.spottrack.platform.monitoring.domain.model.events;

import com.spottrack.platform.monitoring.domain.model.aggregates.Anomaly;

import java.time.LocalDate;

public record EquipmentAnomalyReportSubmitted(
        String reservationId,
        String equipmentId,
        String zoneId,
        String anomalyDescription,
        LocalDate emissionDate) {
    public static EquipmentAnomalyReportSubmitted from(Anomaly anomaly){
        return new EquipmentAnomalyReportSubmitted(
                anomaly.getReservationId().uuid(),
                anomaly.getEquipmentId().uuid(),
                anomaly.getZoneId().uuid(),
                anomaly.getAnomalyDescription(),
                anomaly.getEmissionDate()
        );
    }
}
