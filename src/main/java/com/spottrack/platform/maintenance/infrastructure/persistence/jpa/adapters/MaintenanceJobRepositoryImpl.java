package com.spottrack.platform.maintenance.infrastructure.persistence.jpa.adapters;

import com.spottrack.platform.maintenance.domain.model.aggregates.MaintenanceJob;
import com.spottrack.platform.maintenance.domain.model.valueobjects.MaintenanceJobId;
import com.spottrack.platform.maintenance.domain.repositories.MaintenanceJobRepository;
import com.spottrack.platform.maintenance.infrastructure.persistence.jpa.assemblers.MaintenanceJobPersistenceAssembler;
import com.spottrack.platform.maintenance.infrastructure.persistence.jpa.repositories.MaintenanceJobJpaRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class MaintenanceJobRepositoryImpl implements MaintenanceJobRepository {

    private final MaintenanceJobJpaRepository jpaRepository;
    private final ApplicationEventPublisher eventPublisher;

    public MaintenanceJobRepositoryImpl(MaintenanceJobJpaRepository jpaRepository,
                                        ApplicationEventPublisher eventPublisher) {
        this.jpaRepository = jpaRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public Optional<MaintenanceJob> findById(MaintenanceJobId id) {
        return jpaRepository.findByJobId(id.uuid())
                .map(MaintenanceJobPersistenceAssembler::toDomainFromPersistence);
    }

    @Override
    public MaintenanceJob save(MaintenanceJob job) {
        var entity = MaintenanceJobPersistenceAssembler.toPersistenceFromDomain(job);
        jpaRepository.findByJobId(job.getJobId().uuid())
                .ifPresent(existing -> entity.setId(existing.getId()));
        var saved = jpaRepository.save(entity);
        var domain = MaintenanceJobPersistenceAssembler.toDomainFromPersistence(saved);
        job.domainEvents().forEach(eventPublisher::publishEvent);
        job.clearDomainEvents();
        return domain;
    }
}
