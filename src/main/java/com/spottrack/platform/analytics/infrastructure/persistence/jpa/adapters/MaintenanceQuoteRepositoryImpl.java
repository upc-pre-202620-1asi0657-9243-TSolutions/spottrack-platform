package com.spottrack.platform.analytics.infrastructure.persistence.jpa.adapters;

import com.spottrack.platform.analytics.domain.model.aggregates.MaintenanceQuote;
import com.spottrack.platform.analytics.domain.model.valueobjects.MaintenanceQuoteId;
import com.spottrack.platform.analytics.domain.repositories.MaintenanceQuoteRepository;
import com.spottrack.platform.analytics.infrastructure.persistence.jpa.assemblers.MaintenanceQuotePersistenceAssembler;
import com.spottrack.platform.analytics.infrastructure.persistence.jpa.repositories.JpaMaintenanceQuoteRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public class MaintenanceQuoteRepositoryImpl implements MaintenanceQuoteRepository {

    private final JpaMaintenanceQuoteRepository jpaMaintenanceQuoteRepository;
    private final ApplicationEventPublisher eventPublisher;

    public MaintenanceQuoteRepositoryImpl(JpaMaintenanceQuoteRepository jpaMaintenanceQuoteRepository, ApplicationEventPublisher eventPublisher) {
        this.jpaMaintenanceQuoteRepository = jpaMaintenanceQuoteRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public MaintenanceQuote save(MaintenanceQuote maintenanceQuote) {
        var entity = MaintenanceQuotePersistenceAssembler.toPersistenceFromDomain(maintenanceQuote);
        var savedEntity = jpaMaintenanceQuoteRepository.save(entity);
        maintenanceQuote.setId(savedEntity.getId());
        maintenanceQuote.domainEvents().forEach(eventPublisher::publishEvent);
        maintenanceQuote.clearDomainEvents();
        return maintenanceQuote;
    }

    @Override
    public Optional<MaintenanceQuote> findByMaintenanceQuoteId(MaintenanceQuoteId maintenanceQuoteId) {
        return this.jpaMaintenanceQuoteRepository.findByMaintenanceQuoteId(maintenanceQuoteId)
                .map(MaintenanceQuotePersistenceAssembler::toDomainFromPersistence);
    }

    @Override
    public Optional<MaintenanceQuote> findById(Long id) {
        return this.jpaMaintenanceQuoteRepository.findById(id)
                .map(MaintenanceQuotePersistenceAssembler::toDomainFromPersistence);
    }

    @Override
    public List<MaintenanceQuote> findAll() {
        return this.jpaMaintenanceQuoteRepository.findAll().stream()
                .map(MaintenanceQuotePersistenceAssembler::toDomainFromPersistence)
                .toList();
    }
}
