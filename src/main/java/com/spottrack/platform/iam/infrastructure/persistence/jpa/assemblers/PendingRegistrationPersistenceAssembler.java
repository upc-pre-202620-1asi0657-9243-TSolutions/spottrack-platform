package com.spottrack.platform.iam.infrastructure.persistence.jpa.assemblers;

import com.spottrack.platform.iam.domain.model.aggregates.PendingRegistration;
import com.spottrack.platform.iam.infrastructure.persistence.jpa.entities.PendingRegistrationPersistenceEntity;

public final class PendingRegistrationPersistenceAssembler {

    private PendingRegistrationPersistenceAssembler() {
    }

    public static PendingRegistration toDomainFromPersistence(PendingRegistrationPersistenceEntity entity) {
        return new PendingRegistration(
                entity.getRegistrationId(),
                entity.getEmail(),
                entity.getHashedPassword(),
                entity.getFirstName(),
                entity.getLastName(),
                entity.getPhoneNumber(),
                entity.getDni(),
                entity.getCompanyName(),
                entity.getRuc(),
                entity.getLegalStructure(),
                entity.getCompanyPhone(),
                entity.getCompanyEmail(),
                entity.getStreetAddress(),
                entity.getCity(),
                entity.getDistrict(),
                entity.getMembershipTier(),
                entity.getCreatedAt(),
                entity.getExpiresAt(),
                entity.getStatus()
        );
    }

    public static PendingRegistrationPersistenceEntity toPersistenceFromDomain(PendingRegistration domain) {
        var entity = new PendingRegistrationPersistenceEntity();
        entity.setRegistrationId(domain.getRegistrationId());
        entity.setEmail(domain.getEmail());
        entity.setHashedPassword(domain.getHashedPassword());
        entity.setFirstName(domain.getFirstName());
        entity.setLastName(domain.getLastName());
        entity.setPhoneNumber(domain.getPhoneNumber());
        entity.setDni(domain.getDni());
        entity.setCompanyName(domain.getCompanyName());
        entity.setRuc(domain.getRuc());
        entity.setLegalStructure(domain.getLegalStructure());
        entity.setCompanyPhone(domain.getCompanyPhone());
        entity.setCompanyEmail(domain.getCompanyEmail());
        entity.setStreetAddress(domain.getStreetAddress());
        entity.setCity(domain.getCity());
        entity.setDistrict(domain.getDistrict());
        entity.setMembershipTier(domain.getMembershipTier());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setExpiresAt(domain.getExpiresAt());
        entity.setStatus(domain.getStatus());
        return entity;
    }
}
