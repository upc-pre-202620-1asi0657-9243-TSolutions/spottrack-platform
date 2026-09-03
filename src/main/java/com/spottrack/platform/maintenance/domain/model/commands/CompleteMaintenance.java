package com.spottrack.platform.maintenance.domain.model.commands;

import com.spottrack.platform.maintenance.domain.model.valueobjects.TechnicalTicketId;

public record CompleteMaintenance(TechnicalTicketId ticketId) {

}
