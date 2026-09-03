package com.spottrack.platform.analytics.infrastructure.persistence.jpa.adapters;

import com.spottrack.platform.analytics.domain.model.aggregates.ActivityReport;
import com.spottrack.platform.analytics.domain.model.valueobjects.ActivityReportId;
import com.spottrack.platform.analytics.domain.repositories.ActivityReportRepository;
import com.spottrack.platform.analytics.infrastructure.persistence.jpa.assemblers.ActivityReportPersistenceAssembler;
import com.spottrack.platform.analytics.infrastructure.persistence.jpa.repositories.JpaActivityReportRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public class ActivityReportRepositoryImpl implements ActivityReportRepository {

    private final JpaActivityReportRepository jpaActivityReportRepository;
    private final ApplicationEventPublisher eventPublisher;

    public ActivityReportRepositoryImpl(JpaActivityReportRepository jpaActivityReportRepository, ApplicationEventPublisher eventPublisher) {
        this.jpaActivityReportRepository = jpaActivityReportRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public ActivityReport save(ActivityReport activityReport) {
        var entity = ActivityReportPersistenceAssembler.toPersistenceFromDomain(activityReport);
        var savedEntity = jpaActivityReportRepository.save(entity);
        activityReport.setId(savedEntity.getId());
        activityReport.domainEvents().forEach(eventPublisher::publishEvent);
        activityReport.clearDomainEvents();
        return activityReport;
    }

    @Override
    public Optional<ActivityReport> findByActivityReportId(ActivityReportId activityReportId) {
        return jpaActivityReportRepository.findByActivityReportId(activityReportId)
                .map(ActivityReportPersistenceAssembler::toDomainFromPersistence);
    }

    @Override
    public Optional<ActivityReport> findById(Long id) {
        return jpaActivityReportRepository.findById(id)
                .map(ActivityReportPersistenceAssembler::toDomainFromPersistence);
    }

    @Override
    public Optional<ActivityReport> findByEquipmentId(String equipmentId) {
        return jpaActivityReportRepository.findByEquipmentId(equipmentId)
                .map(ActivityReportPersistenceAssembler::toDomainFromPersistence);
    }

    @Override
    public List<ActivityReport> findAll() {
        return jpaActivityReportRepository.findAll().stream()
                .map(ActivityReportPersistenceAssembler::toDomainFromPersistence)
                .toList();
    }
}
