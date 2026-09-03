package com.spottrack.platform.shared.infrastructure.persistence.jpa.adapters;

import com.spottrack.platform.shared.domain.model.aggregates.Alert;
import com.spottrack.platform.shared.domain.repositories.AlertRepository;
import com.spottrack.platform.shared.infrastructure.persistence.jpa.assemblers.AlertPersistenceAssembler;
import com.spottrack.platform.shared.infrastructure.persistence.jpa.repositories.AlertPersistenceRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class AlertRepositoryImpl implements AlertRepository {

    private final AlertPersistenceRepository alertPersistenceRepository;

    public AlertRepositoryImpl(AlertPersistenceRepository alertPersistenceRepository) {
        this.alertPersistenceRepository = alertPersistenceRepository;
    }

    @Override
    public Optional<Alert> findById(Long id) {
        return alertPersistenceRepository.findById(id)
                .map(AlertPersistenceAssembler::toDomainFromPersistence);
    }

    @Override
    public List<Alert> findAllByAdminUserId(Long adminUserId) {
        return alertPersistenceRepository.findAllByAdminUserIdAndResolvedFalse(adminUserId).stream()
                .map(AlertPersistenceAssembler::toDomainFromPersistence)
                .collect(Collectors.toList());
    }

    @Override
    public Alert save(Alert alert) {
        var savedEntity = alertPersistenceRepository.save(AlertPersistenceAssembler.toPersistenceFromDomain(alert));
        return AlertPersistenceAssembler.toDomainFromPersistence(savedEntity);
    }
}
