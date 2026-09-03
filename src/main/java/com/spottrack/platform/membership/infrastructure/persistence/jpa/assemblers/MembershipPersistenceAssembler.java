package com.spottrack.platform.membership.infrastructure.persistence.jpa.assemblers;

import com.spottrack.platform.membership.domain.model.aggregates.Membership;
import com.spottrack.platform.membership.domain.model.valueobjects.MembershipId;
import com.spottrack.platform.membership.domain.model.valueobjects.MembershipPeriod;
import com.spottrack.platform.membership.infrastructure.persistence.jpa.entities.MembershipPersistenceEntity;

import java.util.UUID;

public final class MembershipPersistenceAssembler {

    private MembershipPersistenceAssembler() {}

    public static Membership toDomainFromPersistence(MembershipPersistenceEntity entity) {
        return new Membership(
                entity.getId(),
                new MembershipId(UUID.fromString(entity.getMembershipId())),
                entity.getClientId(),
                entity.getMembershipTier(),
                entity.getPrice(),
                new MembershipPeriod(entity.getStartDate(), entity.getEndDate()),
                entity.getStatus(),
                entity.isCancelAtPeriodEnd(),
                entity.getPendingDowngradeTier()
        );
    }

    public static MembershipPersistenceEntity toPersistenceFromDomain(Membership membership) {
        var entity = new MembershipPersistenceEntity();
        entity.setId(membership.getId());
        entity.setMembershipId(membership.getMembershipId().uuid().toString());
        entity.setClientId(membership.getClientId());
        entity.setMembershipTier(membership.getMembershipTier());
        entity.setPrice(membership.getPrice());
        entity.setStartDate(membership.getMembershipPeriod().startDate());
        entity.setEndDate(membership.getMembershipPeriod().endDate());
        entity.setStatus(membership.getStatus());
        entity.setCancelAtPeriodEnd(membership.isCancelAtPeriodEnd());
        entity.setPendingDowngradeTier(membership.getPendingDowngradeTier());
        return entity;
    }
}
