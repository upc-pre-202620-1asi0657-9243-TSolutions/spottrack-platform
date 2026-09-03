package com.spottrack.platform.maintenance.interfaces.rest.resources;

import com.spottrack.platform.maintenance.domain.model.valueobjects.MaintenanceStatus;
import com.spottrack.platform.maintenance.domain.model.valueobjects.TicketPriority;
import com.spottrack.platform.maintenance.domain.model.valueobjects.TicketStatus;
import com.spottrack.platform.maintenance.domain.model.valueobjects.TicketType;

import java.time.LocalDateTime;

public record TechnicalTicketResource(
        String id,
        String maintenanceId,
        String equipmentId,
        String technicianId,
        String description,
        TicketPriority priority,
        TicketType type,
        TicketStatus ticketStatus,
        MaintenanceStatus maintenanceStatus,
        LocalDateTime createdAt
) {}
