package com.spottrack.platform.maintenance.application.internal.queryservices;

import com.spottrack.platform.maintenance.application.queryservices.TechnicalTicketQueryService;
import com.spottrack.platform.maintenance.domain.model.aggregates.TechnicalTicket;
import com.spottrack.platform.maintenance.domain.model.queries.GetAllTicketsQuery;
import com.spottrack.platform.maintenance.infrastructure.persistence.jpa.assemblers.TechnicalTicketPersistenceAssembler;
import com.spottrack.platform.maintenance.infrastructure.persistence.jpa.repositories.TechnicalTicketJpaRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TechnicalTicketQueryServiceImpl implements TechnicalTicketQueryService {

    private final TechnicalTicketJpaRepository jpaRepository;

    public TechnicalTicketQueryServiceImpl(TechnicalTicketJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public List<TechnicalTicket> handle(GetAllTicketsQuery query) {
        return jpaRepository.findAll()
                .stream()
                .map(TechnicalTicketPersistenceAssembler::toDomainFromPersistence)
                .toList();
    }
}
