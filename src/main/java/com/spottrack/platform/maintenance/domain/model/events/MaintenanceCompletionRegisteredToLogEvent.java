package com.spottrack.platform.maintenance.domain.model.events;

public record MaintenanceCompletionRegisteredToLogEvent(String logId, String ticketId, String maintenanceId) {}
