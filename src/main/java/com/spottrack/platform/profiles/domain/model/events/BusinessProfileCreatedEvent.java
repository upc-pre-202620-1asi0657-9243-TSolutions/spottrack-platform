package com.spottrack.platform.profiles.domain.model.events;

import com.spottrack.platform.profiles.domain.model.aggregates.BusinessProfile;

public record BusinessProfileCreatedEvent(
        Long businessProfileId,
        Long userId,
        String companyName,
        String ruc
) {
    public static BusinessProfileCreatedEvent from(BusinessProfile profile) {
        return new BusinessProfileCreatedEvent(
                profile.getId(),
                profile.getUserId(),
                profile.getBusinessInfo().companyName(),
                profile.getBusinessInfo().ruc()
        );
    }
}
