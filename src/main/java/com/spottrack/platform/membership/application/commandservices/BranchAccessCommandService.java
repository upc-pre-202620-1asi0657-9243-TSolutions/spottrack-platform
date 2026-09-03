package com.spottrack.platform.membership.application.commandservices;

import com.spottrack.platform.membership.domain.model.aggregates.BranchAccess;
import com.spottrack.platform.membership.domain.model.commands.GrantBranchAccessCommand;
import com.spottrack.platform.shared.application.result.ApplicationError;
import com.spottrack.platform.shared.application.result.Result;

public interface BranchAccessCommandService {
    Result<BranchAccess, ApplicationError> handle(GrantBranchAccessCommand command);
}
