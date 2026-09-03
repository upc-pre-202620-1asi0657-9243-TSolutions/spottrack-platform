package com.spottrack.platform.profiles.domain.repositories;

import com.spottrack.platform.profiles.domain.model.aggregates.BusinessProfile;
import com.spottrack.platform.profiles.domain.model.valueobjects.BusinessProfileId;

import java.util.Optional;

public interface BusinessProfileRepository {
    Optional<BusinessProfile> findById(BusinessProfileId businessProfileId);
    Optional<BusinessProfile> findByUserId(Long userId);
    BusinessProfile save(BusinessProfile businessProfile);
    boolean existsByUserId(Long userId);
}
