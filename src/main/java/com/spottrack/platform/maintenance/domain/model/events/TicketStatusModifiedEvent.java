package com.spottrack.platform.maintenance.domain.model.events;

public record TicketStatusModifiedEvent(String ticketId, String newStatus) {}
