package com.spottrack.platform.analytics.infrastructure.persistence.jpa.adapters;

import com.spottrack.platform.analytics.domain.model.aggregates.ROIProjection;
import com.spottrack.platform.analytics.domain.model.valueobjects.ROIProjectionId;
import com.spottrack.platform.analytics.domain.repositories.ROIProjectionRepository;
import com.spottrack.platform.analytics.infrastructure.persistence.jpa.assemblers.ROIProjectionPersistenceAssembler;
import com.spottrack.platform.analytics.infrastructure.persistence.jpa.repositories.JpaROIProjectionRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public class ROIProjectionRepositoryImpl implements ROIProjectionRepository {

    private final JpaROIProjectionRepository jpaROIProjectionRepository;
    private final ApplicationEventPublisher eventPublisher;

    public ROIProjectionRepositoryImpl(JpaROIProjectionRepository jpaROIProjectionRepository, ApplicationEventPublisher eventPublisher) {
        this.jpaROIProjectionRepository = jpaROIProjectionRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public ROIProjection save(ROIProjection roiProjection) {
        var entity = ROIProjectionPersistenceAssembler.toPersistenceFromDomain(roiProjection);
        var savedEntity = jpaROIProjectionRepository.save(entity);
        roiProjection.setId(savedEntity.getId());
        roiProjection.domainEvents().forEach(eventPublisher::publishEvent);
        roiProjection.clearDomainEvents();
        return roiProjection;
    }

    @Override
    public Optional<ROIProjection> findByRoiProjectionId(ROIProjectionId roiProjectionId) {
        return this.jpaROIProjectionRepository.findByRoiProjectionId(roiProjectionId)
                .map(ROIProjectionPersistenceAssembler::toDomainFromPersistence);
    }

    @Override
    public Optional<ROIProjection> findById(Long id) {
        return this.jpaROIProjectionRepository.findById(id)
                .map(ROIProjectionPersistenceAssembler::toDomainFromPersistence);
    }

    @Override
    public List<ROIProjection> findAll() {
        return this.jpaROIProjectionRepository.findAll().stream()
                .map(ROIProjectionPersistenceAssembler::toDomainFromPersistence)
                .toList();
    }
}
