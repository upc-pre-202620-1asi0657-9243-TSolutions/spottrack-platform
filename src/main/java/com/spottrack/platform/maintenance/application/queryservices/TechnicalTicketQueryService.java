package com.spottrack.platform.maintenance.application.queryservices;

import com.spottrack.platform.maintenance.domain.model.aggregates.TechnicalTicket;
import com.spottrack.platform.maintenance.domain.model.queries.GetAllTicketsQuery;

import java.util.List;

public interface TechnicalTicketQueryService {
    List<TechnicalTicket> handle(GetAllTicketsQuery query);
}
