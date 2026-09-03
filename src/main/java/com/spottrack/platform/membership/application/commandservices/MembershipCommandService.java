package com.spottrack.platform.membership.application.commandservices;

import com.spottrack.platform.membership.domain.model.aggregates.Membership;
import com.spottrack.platform.membership.domain.model.commands.ActivateMembershipCommand;
import com.spottrack.platform.membership.domain.model.commands.CancelMembershipCommand;
import com.spottrack.platform.membership.domain.model.commands.CreateMembershipCommand;
import com.spottrack.platform.membership.domain.model.commands.RenewMembershipCommand;
import com.spottrack.platform.membership.domain.model.commands.RequestDowngradePlanCommand;
import com.spottrack.platform.membership.domain.model.commands.SuspendMembershipCommand;
import com.spottrack.platform.membership.domain.model.commands.UndoCancellationCommand;
import com.spottrack.platform.membership.domain.model.commands.UpgradeMembershipPlanCommand;
import com.spottrack.platform.shared.application.result.ApplicationError;
import com.spottrack.platform.shared.application.result.Result;

public interface MembershipCommandService {
    Result<Membership, ApplicationError> handle(CreateMembershipCommand command);
    Result<Membership, ApplicationError> handle(CancelMembershipCommand command);
    Result<Membership, ApplicationError> handle(UndoCancellationCommand command);
    Result<Membership, ApplicationError> handle(RequestDowngradePlanCommand command);
    Result<Membership, ApplicationError> handle(UpgradeMembershipPlanCommand command);
    Result<Membership, ApplicationError> handle(ActivateMembershipCommand command);
    Result<Membership, ApplicationError> handle(SuspendMembershipCommand command);
    Result<Membership, ApplicationError> handle(RenewMembershipCommand command);
}
