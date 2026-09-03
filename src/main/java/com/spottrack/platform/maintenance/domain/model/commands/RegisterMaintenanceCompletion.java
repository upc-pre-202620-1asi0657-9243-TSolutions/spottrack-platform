package com.spottrack.platform.maintenance.domain.model.commands;

import com.spottrack.platform.maintenance.domain.model.valueobjects.MaintenanceId;
import com.spottrack.platform.maintenance.domain.model.valueobjects.TechnicalTicketId;

import java.math.BigDecimal;

public record RegisterMaintenanceCompletion(
        TechnicalTicketId ticketId,
        MaintenanceId maintenanceId,
        String notes,
        BigDecimal cost
) {
    public RegisterMaintenanceCompletion {
        if (notes == null || notes.isBlank())
            throw new IllegalArgumentException("maintenance.command.registerCompletion.notes.notBlank");
        if (cost == null || cost.signum() < 0)
            throw new IllegalArgumentException("maintenance.command.registerCompletion.cost.notNegative");
    }
}
