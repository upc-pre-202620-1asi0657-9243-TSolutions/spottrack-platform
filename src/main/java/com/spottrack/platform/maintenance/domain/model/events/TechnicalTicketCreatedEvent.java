package com.spottrack.platform.maintenance.domain.model.events;

public record TechnicalTicketCreatedEvent(String ticketId, String maintenanceId, String equipmentId) {}
