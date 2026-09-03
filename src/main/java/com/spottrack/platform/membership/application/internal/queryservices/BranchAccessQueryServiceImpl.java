package com.spottrack.platform.membership.application.internal.queryservices;

import com.spottrack.platform.membership.application.queryservices.BranchAccessQueryService;
import com.spottrack.platform.membership.domain.model.aggregates.BranchAccess;
import com.spottrack.platform.membership.domain.model.queries.GetBranchAccessByIdQuery;
import com.spottrack.platform.membership.domain.model.queries.GetBranchAccessesByMembershipIdQuery;
import com.spottrack.platform.membership.domain.repositories.BranchAccessRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class BranchAccessQueryServiceImpl implements BranchAccessQueryService {

    private final BranchAccessRepository branchAccessRepository;

    public BranchAccessQueryServiceImpl(BranchAccessRepository branchAccessRepository) {
        this.branchAccessRepository = branchAccessRepository;
    }

    @Override
    public Optional<BranchAccess> handle(GetBranchAccessByIdQuery query) {
        return branchAccessRepository.findByBranchAccessId(query.branchAccessId());
    }

    @Override
    public List<BranchAccess> handle(GetBranchAccessesByMembershipIdQuery query) {
        return branchAccessRepository.findByMembershipId(query.membershipId());
    }
}
