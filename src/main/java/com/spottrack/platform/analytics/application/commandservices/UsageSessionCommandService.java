package com.spottrack.platform.analytics.application.commandservices;

import com.spottrack.platform.analytics.domain.model.aggregates.UsageSession;
import com.spottrack.platform.analytics.domain.model.commands.RecordUsageSessionCommand;
import com.spottrack.platform.shared.application.result.ApplicationError;
import com.spottrack.platform.shared.application.result.Result;

public interface UsageSessionCommandService {
    Result<UsageSession, ApplicationError> handle(RecordUsageSessionCommand command);
}
