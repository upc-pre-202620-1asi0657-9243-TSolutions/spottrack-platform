package com.spottrack.platform.maintenance.domain.model.events;

public record MaintenanceStatusUpdateRequestedEvent(String ticketId, String requestedStatus) {}
