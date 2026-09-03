package com.spottrack.platform.membership.application.internal.queryservices;

import com.spottrack.platform.membership.application.queryservices.MembershipQueryService;
import com.spottrack.platform.membership.domain.model.aggregates.Membership;
import com.spottrack.platform.membership.domain.model.queries.GetMembershipByIdQuery;
import com.spottrack.platform.membership.domain.model.queries.GetMembershipsByClientIdQuery;
import com.spottrack.platform.membership.domain.model.queries.GetPrimaryMembershipByClientIdQuery;
import com.spottrack.platform.membership.domain.model.valueobjects.MembershipStatus;
import com.spottrack.platform.membership.domain.repositories.MembershipRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class MembershipQueryServiceImpl implements MembershipQueryService {

    private final MembershipRepository membershipRepository;

    public MembershipQueryServiceImpl(MembershipRepository membershipRepository) {
        this.membershipRepository = membershipRepository;
    }

    @Override
    public Optional<Membership> handle(GetMembershipByIdQuery query) {
        return membershipRepository.findByMembershipId(query.membershipId());
    }

    @Override
    public List<Membership> handle(GetMembershipsByClientIdQuery query) {
        return membershipRepository.findByClientId(query.clientId());
    }

    @Override
    public Optional<Membership> handle(GetPrimaryMembershipByClientIdQuery query) {
        var all = membershipRepository.findByClientId(query.clientId());
        // Priority 1: the one live membership (ACTIVE or SUSPENDED) — at most one can
        // exist at a time by domain invariant.
        return all.stream()
                .filter(m -> m.getStatus() == MembershipStatus.ACTIVE
                        || m.getStatus() == MembershipStatus.SUSPENDED)
                .findFirst()
                // Priority 2: no live membership — return the most recently created one
                // (highest auto-increment id is a reliable proxy for created_at order).
                // Business decision: shows the last known state so the frontend can
                // offer resubscribe instead of a blank screen.
                .or(() -> all.stream()
                        .max(java.util.Comparator.comparingLong(Membership::getId)));
    }
}
