package com.spottrack.platform.profiles.application.commandservices;

import com.spottrack.platform.profiles.domain.model.aggregates.Admin;
import com.spottrack.platform.profiles.domain.model.commands.CreateAdminCommand;
import com.spottrack.platform.profiles.domain.model.commands.UpdateAdminProfileCommand;
import com.spottrack.platform.shared.application.result.ApplicationError;
import com.spottrack.platform.shared.application.result.Result;

public interface AdminCommandService {
    Result<Admin, ApplicationError> handle(CreateAdminCommand command);
    Result<Admin, ApplicationError> handle(UpdateAdminProfileCommand command);
}
