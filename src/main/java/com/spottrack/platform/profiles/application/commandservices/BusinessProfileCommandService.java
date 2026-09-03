package com.spottrack.platform.profiles.application.commandservices;

import com.spottrack.platform.profiles.domain.model.aggregates.BusinessProfile;
import com.spottrack.platform.profiles.domain.model.commands.CreateBusinessProfileCommand;
import com.spottrack.platform.shared.application.result.ApplicationError;
import com.spottrack.platform.shared.application.result.Result;

public interface BusinessProfileCommandService {
    Result<BusinessProfile, ApplicationError> handle(CreateBusinessProfileCommand command);
}
