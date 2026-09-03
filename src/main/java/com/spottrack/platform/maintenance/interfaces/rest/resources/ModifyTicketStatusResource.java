package com.spottrack.platform.maintenance.interfaces.rest.resources;

import com.spottrack.platform.maintenance.domain.model.valueobjects.TicketStatus;

public record ModifyTicketStatusResource(TicketStatus newStatus) {}
