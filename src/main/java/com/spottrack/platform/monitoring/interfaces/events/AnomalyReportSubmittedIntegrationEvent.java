package com.spottrack.platform.monitoring.interfaces.events;

import java.time.LocalDate;

public record AnomalyReportSubmittedIntegrationEvent(
        String reservationId,
        String equipmentId,
        String zoneId,
        String anomalyDescription,
        LocalDate emissionDate
) {
}
