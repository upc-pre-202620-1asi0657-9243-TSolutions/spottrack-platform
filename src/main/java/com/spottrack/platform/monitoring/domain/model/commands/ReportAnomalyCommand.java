package com.spottrack.platform.monitoring.domain.model.commands;

public record ReportAnomalyCommand(String reservationId, String equipmentId, String zoneId, String anomalyDescription) {

}
