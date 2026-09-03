package com.spottrack.platform.maintenance.infrastructure.persistence.jpa.adapters;

import com.spottrack.platform.maintenance.domain.model.aggregates.TechnicalTicket;
import com.spottrack.platform.maintenance.domain.model.valueobjects.TechnicalTicketId;
import com.spottrack.platform.maintenance.domain.repositories.TechnicalTicketRepository;
import com.spottrack.platform.maintenance.infrastructure.persistence.jpa.assemblers.TechnicalTicketPersistenceAssembler;
import com.spottrack.platform.maintenance.infrastructure.persistence.jpa.repositories.TechnicalTicketJpaRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class TechnicalTicketRepositoryImpl implements TechnicalTicketRepository {

    private final TechnicalTicketJpaRepository jpaRepository;
    private final ApplicationEventPublisher eventPublisher;

    public TechnicalTicketRepositoryImpl(TechnicalTicketJpaRepository jpaRepository,
                                         ApplicationEventPublisher eventPublisher) {
        this.jpaRepository = jpaRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public Optional<TechnicalTicket> findById(TechnicalTicketId id) {
        return jpaRepository.findByTicketId(id.uuid())
                .map(TechnicalTicketPersistenceAssembler::toDomainFromPersistence);
    }

    @Override
    public Optional<TechnicalTicket> findByMaintenanceId(String maintenanceId) {
        return jpaRepository.findByMaintenanceId(maintenanceId)
                .map(TechnicalTicketPersistenceAssembler::toDomainFromPersistence);
    }

    @Override
    public TechnicalTicket save(TechnicalTicket ticket) {
        var entity = TechnicalTicketPersistenceAssembler.toPersistenceFromDomain(ticket);
        jpaRepository.findByTicketId(ticket.getTicketId().uuid())
                .ifPresent(existing -> entity.setId(existing.getId()));
        var saved = jpaRepository.save(entity);
        var domain = TechnicalTicketPersistenceAssembler.toDomainFromPersistence(saved);
        ticket.domainEvents().forEach(eventPublisher::publishEvent);
        ticket.clearDomainEvents();
        return domain;
    }
}
