package com.spottrack.platform.maintenance.infrastructure.persistence.jpa.adapters;

import com.spottrack.platform.maintenance.domain.model.aggregates.Maintenance;
import com.spottrack.platform.maintenance.domain.model.valueobjects.MaintenanceId;
import com.spottrack.platform.maintenance.domain.repositories.MaintenanceRepository;
import com.spottrack.platform.maintenance.infrastructure.persistence.jpa.assemblers.MaintenancePersistenceAssembler;
import com.spottrack.platform.maintenance.infrastructure.persistence.jpa.entities.MaintenancePersistenceEntity;
import com.spottrack.platform.maintenance.infrastructure.persistence.jpa.repositories.MaintenancePersistenceRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class MaintenanceRepositoryImpl implements MaintenanceRepository {

    private final MaintenancePersistenceRepository maintenancePersistenceRepository;
    private final ApplicationEventPublisher eventPublisher;

    public MaintenanceRepositoryImpl(
            MaintenancePersistenceRepository maintenancePersistenceRepository,
            ApplicationEventPublisher eventPublisher) {
        this.maintenancePersistenceRepository = maintenancePersistenceRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public Optional<Maintenance> findByMaintenanceId(MaintenanceId maintenanceId) {
        return maintenancePersistenceRepository
                .findByMaintenanceId(maintenanceId.uuid())
                .map(MaintenancePersistenceAssembler::toDomainFromPersistence);
    }

    @Override
    public Optional<Maintenance> findById(Long id) {
        return maintenancePersistenceRepository
                .findById(id)
                .map(MaintenancePersistenceAssembler::toDomainFromPersistence);
    }

    @Override
    public List<Maintenance> findAll() {
        return maintenancePersistenceRepository.findAll().stream()
                .map(MaintenancePersistenceAssembler::toDomainFromPersistence)
                .toList();
    }

    @Override
    public Maintenance save(Maintenance maintenance) {
        var persistenceEntity = MaintenancePersistenceAssembler.toPersistenceFromDomain(maintenance);
        var savedEntity = maintenancePersistenceRepository.save(persistenceEntity);
        var saved = MaintenancePersistenceAssembler.toDomainFromPersistence(savedEntity);
        maintenance.domainEvents().forEach(eventPublisher::publishEvent);
        maintenance.clearDomainEvents();
        return saved;
    }
}
