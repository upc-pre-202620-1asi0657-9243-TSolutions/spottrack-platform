package com.spottrack.platform.membership.domain.repositories;

import com.spottrack.platform.membership.domain.model.aggregates.BranchAccess;
import com.spottrack.platform.membership.domain.model.valueobjects.BranchAccessId;

import java.util.List;
import java.util.Optional;

public interface BranchAccessRepository {
    Optional<BranchAccess> findByBranchAccessId(BranchAccessId branchAccessId);
    List<BranchAccess> findByMembershipId(Long membershipId);
    BranchAccess save(BranchAccess branchAccess);
}
