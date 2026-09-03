package com.spottrack.platform.maintenance.infrastructure.persistence.jpa.adapters;

import com.spottrack.platform.maintenance.domain.model.aggregates.MaintenanceLog;
import com.spottrack.platform.maintenance.domain.repositories.MaintenanceLogRepository;
import com.spottrack.platform.maintenance.infrastructure.persistence.jpa.assemblers.MaintenanceLogPersistenceAssembler;
import com.spottrack.platform.maintenance.infrastructure.persistence.jpa.repositories.MaintenanceLogJpaRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class MaintenanceLogRepositoryImpl implements MaintenanceLogRepository {

    private final MaintenanceLogJpaRepository jpaRepository;
    private final ApplicationEventPublisher eventPublisher;

    public MaintenanceLogRepositoryImpl(MaintenanceLogJpaRepository jpaRepository,
                                        ApplicationEventPublisher eventPublisher) {
        this.jpaRepository = jpaRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public MaintenanceLog save(MaintenanceLog log) {
        var entity = MaintenanceLogPersistenceAssembler.toPersistenceFromDomain(log);
        var saved = jpaRepository.save(entity);
        var domain = MaintenanceLogPersistenceAssembler.toDomainFromPersistence(saved);
        log.domainEvents().forEach(eventPublisher::publishEvent);
        log.clearDomainEvents();
        return domain;
    }

    @Override
    public List<MaintenanceLog> findByTicketId(String ticketId) {
        return jpaRepository.findByTicketIdOrderByCompletedAtDesc(ticketId).stream()
                .map(MaintenanceLogPersistenceAssembler::toDomainFromPersistence)
                .toList();
    }

    @Override
    public List<MaintenanceLog> findAll() {
        return jpaRepository.findAll().stream()
                .map(MaintenanceLogPersistenceAssembler::toDomainFromPersistence)
                .toList();
    }
}
