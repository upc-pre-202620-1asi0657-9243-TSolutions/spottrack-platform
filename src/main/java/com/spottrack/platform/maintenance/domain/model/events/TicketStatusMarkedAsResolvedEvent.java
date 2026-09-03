package com.spottrack.platform.maintenance.domain.model.events;

public record TicketStatusMarkedAsResolvedEvent(String ticketId, String equipmentId) {}
