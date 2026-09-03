package com.spottrack.platform.membership.domain.model.queries;

import com.spottrack.platform.membership.domain.model.valueobjects.BranchAccessId;

public record GetBranchAccessByIdQuery(BranchAccessId branchAccessId) {
    public GetBranchAccessByIdQuery {
        if (branchAccessId == null) throw new IllegalArgumentException("membership.error.branchAccessId.notNull");
    }
}
