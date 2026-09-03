package com.spottrack.platform.iam.application.commandservices;

import com.spottrack.platform.iam.domain.model.commands.SavePendingRegistrationCommand;
import com.spottrack.platform.shared.application.result.ApplicationError;
import com.spottrack.platform.shared.application.result.Result;

import java.util.UUID;

public interface PendingRegistrationCommandService {
    Result<UUID, ApplicationError> handle(SavePendingRegistrationCommand command);
}
