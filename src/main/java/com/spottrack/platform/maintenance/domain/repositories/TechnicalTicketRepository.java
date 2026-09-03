package com.spottrack.platform.maintenance.domain.repositories;

import com.spottrack.platform.maintenance.domain.model.aggregates.TechnicalTicket;
import com.spottrack.platform.maintenance.domain.model.valueobjects.TechnicalTicketId;

import java.util.Optional;

public interface TechnicalTicketRepository {
    Optional<TechnicalTicket> findById(TechnicalTicketId id);
    Optional<TechnicalTicket> findByMaintenanceId(String maintenanceId);
    TechnicalTicket save(TechnicalTicket ticket);
}
