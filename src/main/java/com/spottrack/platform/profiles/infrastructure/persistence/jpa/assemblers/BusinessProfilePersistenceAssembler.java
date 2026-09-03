package com.spottrack.platform.profiles.infrastructure.persistence.jpa.assemblers;

import com.spottrack.platform.profiles.domain.model.aggregates.BusinessProfile;
import com.spottrack.platform.profiles.domain.model.valueobjects.BusinessInfo;
import com.spottrack.platform.profiles.domain.model.valueobjects.EmailAddress;
import com.spottrack.platform.profiles.domain.model.valueobjects.PhoneNumber;
import com.spottrack.platform.profiles.infrastructure.persistence.jpa.embeddables.BusinessInfoPersistenceEmbeddable;
import com.spottrack.platform.profiles.infrastructure.persistence.jpa.entities.BusinessProfilePersistenceEntity;

public final class BusinessProfilePersistenceAssembler {

    private BusinessProfilePersistenceAssembler() {
    }

    public static BusinessProfile toDomainFromPersistence(BusinessProfilePersistenceEntity entity) {
        return new BusinessProfile(
                entity.getId(),
                entity.getUserId(),
                toDomainBusinessInfo(entity.getBusinessInfo())
        );
    }

    public static BusinessProfilePersistenceEntity toPersistenceFromDomain(BusinessProfile profile) {
        var entity = new BusinessProfilePersistenceEntity();
        entity.setId(profile.getId());
        entity.setUserId(profile.getUserId());
        entity.setBusinessInfo(toPersistenceBusinessInfo(profile.getBusinessInfo()));
        return entity;
    }

    private static BusinessInfo toDomainBusinessInfo(BusinessInfoPersistenceEmbeddable embeddable) {
        if (embeddable == null || embeddable.getCompanyName() == null) return null;
        return new BusinessInfo(
                embeddable.getCompanyName(),
                embeddable.getRuc(),
                embeddable.getLegalStructure(),
                new PhoneNumber(embeddable.getCompanyPhone()),
                new EmailAddress(embeddable.getCompanyEmail()),
                embeddable.getStreetAddress(),
                embeddable.getCity(),
                embeddable.getDistrict()
        );
    }

    private static BusinessInfoPersistenceEmbeddable toPersistenceBusinessInfo(BusinessInfo info) {
        if (info == null) return null;
        return new BusinessInfoPersistenceEmbeddable(
                info.companyName(),
                info.ruc(),
                info.legalStructure(),
                info.companyPhone().phoneNumber(),
                info.companyEmail().address(),
                info.streetAddress(),
                info.city(),
                info.district()
        );
    }
}
