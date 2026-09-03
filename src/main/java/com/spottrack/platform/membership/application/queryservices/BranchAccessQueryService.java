package com.spottrack.platform.membership.application.queryservices;

import com.spottrack.platform.membership.domain.model.aggregates.BranchAccess;
import com.spottrack.platform.membership.domain.model.queries.GetBranchAccessByIdQuery;
import com.spottrack.platform.membership.domain.model.queries.GetBranchAccessesByMembershipIdQuery;

import java.util.List;
import java.util.Optional;

public interface BranchAccessQueryService {
    Optional<BranchAccess> handle(GetBranchAccessByIdQuery query);
    List<BranchAccess> handle(GetBranchAccessesByMembershipIdQuery query);
}
