package com.spottrack.platform.maintenance.domain.model.events;

public record TechnicalTicketAssignedEvent(String ticketId, String technicianId, String equipmentId) {}
