package com.spottrack.platform.notification.infrastructure.persistence.jpa.adapters;

import com.spottrack.platform.notification.domain.model.aggregates.EquipmentWatch;
import com.spottrack.platform.notification.domain.model.valueobjects.WatchOrigin;
import com.spottrack.platform.notification.domain.model.valueobjects.WatchStatus;
import com.spottrack.platform.notification.domain.repositories.EquipmentWatchRepository;
import com.spottrack.platform.notification.infrastructure.persistence.jpa.assemblers.EquipmentWatchPersistenceAssembler;
import com.spottrack.platform.notification.infrastructure.persistence.jpa.repositories.EquipmentWatchPersistenceRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class EquipmentWatchRepositoryImpl implements EquipmentWatchRepository {

    private final EquipmentWatchPersistenceRepository persistenceRepository;

    public EquipmentWatchRepositoryImpl(EquipmentWatchPersistenceRepository persistenceRepository) {
        this.persistenceRepository = persistenceRepository;
    }

    @Override
    public Optional<EquipmentWatch> findActiveByClientIdAndEquipmentIdAndOrigin(Long clientId, String equipmentId, WatchOrigin origin) {
        return persistenceRepository
                .findFirstByClientIdAndEquipmentIdAndOriginAndStatus(clientId, equipmentId, origin, WatchStatus.ACTIVE)
                .map(EquipmentWatchPersistenceAssembler::toDomainFromPersistence);
    }

    @Override
    public List<EquipmentWatch> findAllByClientIdAndStatus(Long clientId, WatchStatus status) {
        return persistenceRepository.findAllByClientIdAndStatus(clientId, status).stream()
                .map(EquipmentWatchPersistenceAssembler::toDomainFromPersistence)
                .toList();
    }

    @Override
    public List<EquipmentWatch> findAllByEquipmentIdAndStatus(String equipmentId, WatchStatus status) {
        return persistenceRepository.findAllByEquipmentIdAndStatus(equipmentId, status).stream()
                .map(EquipmentWatchPersistenceAssembler::toDomainFromPersistence)
                .toList();
    }

    @Override
    public EquipmentWatch save(EquipmentWatch watch) {
        var saved = persistenceRepository.save(EquipmentWatchPersistenceAssembler.toPersistenceFromDomain(watch));
        return EquipmentWatchPersistenceAssembler.toDomainFromPersistence(saved);
    }
}
