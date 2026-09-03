package com.spottrack.platform.monitoring.infrastructure.persistence.jpa.adapters;

import com.spottrack.platform.monitoring.domain.model.aggregates.Anomaly;
import com.spottrack.platform.monitoring.domain.repositories.AnomalyRepository;
import com.spottrack.platform.monitoring.infrastructure.persistence.jpa.assemblers.AnomalyPersistenceAssembler;
import com.spottrack.platform.monitoring.infrastructure.persistence.jpa.repositories.AnomalyPersistenceRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Repository;

@Repository
public class AnomalyRepositoryImpl implements AnomalyRepository {
    private final AnomalyPersistenceRepository anomalyPersistenceRepository;
    private final ApplicationEventPublisher eventPublisher;

    public AnomalyRepositoryImpl(AnomalyPersistenceRepository anomalyPersistenceRepository, ApplicationEventPublisher eventPublisher){
        this.anomalyPersistenceRepository = anomalyPersistenceRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public Anomaly save(Anomaly anomaly) {
        boolean isNew = anomaly.getId() == null;
        var entity = AnomalyPersistenceAssembler.toPersistenceFromDomain(anomaly);
        var saved = anomalyPersistenceRepository.save(entity);
        var domain = AnomalyPersistenceAssembler.toDomainFromPersistence(saved);
        if (isNew) {
            anomaly.onCreated();
            anomaly.domainEvents().forEach(eventPublisher::publishEvent);
            anomaly.clearDomainEvents();
        }
        return domain;
    }
}
