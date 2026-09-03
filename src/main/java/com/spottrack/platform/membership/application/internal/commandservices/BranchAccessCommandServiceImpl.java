package com.spottrack.platform.membership.application.internal.commandservices;

import com.spottrack.platform.membership.application.commandservices.BranchAccessCommandService;
import com.spottrack.platform.membership.domain.model.aggregates.BranchAccess;
import com.spottrack.platform.membership.domain.model.commands.GrantBranchAccessCommand;
import com.spottrack.platform.membership.domain.model.valueobjects.BranchAccessStatus;
import com.spottrack.platform.membership.domain.model.valueobjects.MembershipStatus;
import com.spottrack.platform.membership.domain.repositories.BranchAccessRepository;
import com.spottrack.platform.membership.domain.repositories.MembershipRepository;
import com.spottrack.platform.shared.application.result.ApplicationError;
import com.spottrack.platform.shared.application.result.Result;
import org.springframework.stereotype.Service;

@Service
public class BranchAccessCommandServiceImpl implements BranchAccessCommandService {

    private final BranchAccessRepository branchAccessRepository;
    private final MembershipRepository membershipRepository;

    public BranchAccessCommandServiceImpl(
            BranchAccessRepository branchAccessRepository,
            MembershipRepository membershipRepository) {
        this.branchAccessRepository = branchAccessRepository;
        this.membershipRepository = membershipRepository;
    }

    @Override
    public Result<BranchAccess, ApplicationError> handle(GrantBranchAccessCommand command) {
        try {
            var membership = membershipRepository.findById(command.membershipId());
            if (membership.isEmpty()) {
                return Result.failure(ApplicationError.notFound("Membership", command.membershipId().toString()));
            }
            var status = membership.get().getStatus() == MembershipStatus.ACTIVE
                    ? BranchAccessStatus.GRANTED
                    : BranchAccessStatus.DENIED;
            var branchAccess = new BranchAccess(command, status);
            var saved = branchAccessRepository.save(branchAccess);
            return Result.success(saved);
        } catch (IllegalArgumentException e) {
            return Result.failure(ApplicationError.validationError("BranchAccess", e.getMessage()));
        } catch (Exception e) {
            return Result.failure(ApplicationError.unexpected("BranchAccess grant", e.getMessage()));
        }
    }
}
