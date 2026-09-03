package com.spottrack.platform.maintenance.infrastructure.persistence.jpa.adapters;

import com.spottrack.platform.maintenance.domain.model.aggregates.Technician;
import com.spottrack.platform.maintenance.domain.model.valueobjects.TechnicianId;
import com.spottrack.platform.maintenance.domain.repositories.TechnicianRepository;
import com.spottrack.platform.maintenance.infrastructure.persistence.jpa.assemblers.TechnicianPersistenceAssembler;
import com.spottrack.platform.maintenance.infrastructure.persistence.jpa.repositories.TechnicianJpaRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class TechnicianRepositoryImpl implements TechnicianRepository {

    private final TechnicianJpaRepository jpaRepository;
    private final ApplicationEventPublisher eventPublisher;

    public TechnicianRepositoryImpl(TechnicianJpaRepository jpaRepository,
                                    ApplicationEventPublisher eventPublisher) {
        this.jpaRepository = jpaRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public Optional<Technician> findById(TechnicianId id) {
        return jpaRepository.findByTechnicianId(id.uuid())
                .map(TechnicianPersistenceAssembler::toDomainFromPersistence);
    }

    @Override
    public boolean existsById(TechnicianId id) {
        return jpaRepository.existsByTechnicianId(id.uuid());
    }

    @Override
    public List<Technician> findAll() {
        return jpaRepository.findAll().stream()
                .map(TechnicianPersistenceAssembler::toDomainFromPersistence)
                .toList();
    }

    @Override
    public Technician save(Technician technician) {
        var entity = TechnicianPersistenceAssembler.toPersistenceFromDomain(technician);
        jpaRepository.findByTechnicianId(technician.getTechnicianId().uuid())
                .ifPresent(existing -> entity.setId(existing.getId()));
        var saved = jpaRepository.save(entity);
        var domain = TechnicianPersistenceAssembler.toDomainFromPersistence(saved);
        technician.domainEvents().forEach(eventPublisher::publishEvent);
        technician.clearDomainEvents();
        return domain;
    }
}
