package com.spottrack.platform.iam.infrastructure.persistence.jpa.adapters;

import com.spottrack.platform.iam.domain.model.aggregates.PendingRegistration;
import com.spottrack.platform.iam.domain.model.valueobjects.PendingRegistrationStatus;
import com.spottrack.platform.iam.domain.repositories.PendingRegistrationRepository;
import com.spottrack.platform.iam.infrastructure.persistence.jpa.assemblers.PendingRegistrationPersistenceAssembler;
import com.spottrack.platform.iam.infrastructure.persistence.jpa.repositories.PendingRegistrationPersistenceRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public class PendingRegistrationRepositoryImpl implements PendingRegistrationRepository {

    private final PendingRegistrationPersistenceRepository persistenceRepository;

    public PendingRegistrationRepositoryImpl(PendingRegistrationPersistenceRepository persistenceRepository) {
        this.persistenceRepository = persistenceRepository;
    }

    @Override
    public PendingRegistration save(PendingRegistration pendingRegistration) {
        var entity = PendingRegistrationPersistenceAssembler.toPersistenceFromDomain(pendingRegistration);
        var saved = persistenceRepository.save(entity);
        return PendingRegistrationPersistenceAssembler.toDomainFromPersistence(saved);
    }

    @Override
    public Optional<PendingRegistration> findById(UUID registrationId) {
        return persistenceRepository.findById(registrationId)
                .map(PendingRegistrationPersistenceAssembler::toDomainFromPersistence);
    }

    @Override
    public Optional<PendingRegistration> findByEmail(String email) {
        return persistenceRepository.findByEmail(email)
                .map(PendingRegistrationPersistenceAssembler::toDomainFromPersistence);
    }

    @Override
    public boolean existsActivePendingByEmail(String email) {
        return persistenceRepository.existsByEmailAndStatusAndExpiresAtAfter(
                email, PendingRegistrationStatus.PENDING, LocalDateTime.now()
        );
    }

    @Override
    @Transactional
    public void deleteExpiredPending() {
        persistenceRepository.deleteByStatusAndExpiresAtBefore(
                PendingRegistrationStatus.PENDING, LocalDateTime.now()
        );
    }
}
