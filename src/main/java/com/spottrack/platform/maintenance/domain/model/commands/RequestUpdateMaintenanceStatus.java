package com.spottrack.platform.maintenance.domain.model.commands;

import com.spottrack.platform.maintenance.domain.model.valueobjects.MaintenanceStatus;
import com.spottrack.platform.maintenance.domain.model.valueobjects.TechnicalTicketId;

public record RequestUpdateMaintenanceStatus(
        TechnicalTicketId ticketId,
        MaintenanceStatus newStatus
) {
}
