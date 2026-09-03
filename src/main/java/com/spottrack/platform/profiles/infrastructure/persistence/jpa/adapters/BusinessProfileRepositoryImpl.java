package com.spottrack.platform.profiles.infrastructure.persistence.jpa.adapters;

import com.spottrack.platform.profiles.domain.model.aggregates.BusinessProfile;
import com.spottrack.platform.profiles.domain.model.valueobjects.BusinessProfileId;
import com.spottrack.platform.profiles.domain.repositories.BusinessProfileRepository;
import com.spottrack.platform.profiles.infrastructure.persistence.jpa.assemblers.BusinessProfilePersistenceAssembler;
import com.spottrack.platform.profiles.infrastructure.persistence.jpa.repositories.BusinessProfilePersistenceRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class BusinessProfileRepositoryImpl implements BusinessProfileRepository {

    private final BusinessProfilePersistenceRepository businessProfilePersistenceRepository;
    private final ApplicationEventPublisher eventPublisher;

    public BusinessProfileRepositoryImpl(
            BusinessProfilePersistenceRepository businessProfilePersistenceRepository,
            ApplicationEventPublisher eventPublisher) {
        this.businessProfilePersistenceRepository = businessProfilePersistenceRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public Optional<BusinessProfile> findById(BusinessProfileId businessProfileId) {
        return businessProfilePersistenceRepository.findById(businessProfileId.businessProfileId())
                .map(BusinessProfilePersistenceAssembler::toDomainFromPersistence);
    }

    @Override
    public Optional<BusinessProfile> findByUserId(Long userId) {
        return businessProfilePersistenceRepository.findByUserId(userId)
                .map(BusinessProfilePersistenceAssembler::toDomainFromPersistence);
    }

    @Override
    public BusinessProfile save(BusinessProfile businessProfile) {
        boolean isNew = businessProfile.getId() == null;
        var savedEntity = businessProfilePersistenceRepository.save(
                BusinessProfilePersistenceAssembler.toPersistenceFromDomain(businessProfile));
        var savedProfile = BusinessProfilePersistenceAssembler.toDomainFromPersistence(savedEntity);
        if (isNew) {
            savedProfile.onCreated();
            savedProfile.domainEvents().forEach(eventPublisher::publishEvent);
            savedProfile.clearDomainEvents();
        }
        return savedProfile;
    }

    @Override
    public boolean existsByUserId(Long userId) {
        return businessProfilePersistenceRepository.existsByUserId(userId);
    }
}
