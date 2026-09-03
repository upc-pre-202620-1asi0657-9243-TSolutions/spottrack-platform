package com.spottrack.platform.maintenance.domain.model.events;

public record MaintenanceStatusUpdatedEvent(String ticketId, String newStatus) {}
