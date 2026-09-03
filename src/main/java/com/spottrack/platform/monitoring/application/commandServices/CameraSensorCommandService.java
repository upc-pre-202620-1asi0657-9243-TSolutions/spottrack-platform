package com.spottrack.platform.monitoring.application.commandServices;

import com.spottrack.platform.monitoring.domain.model.aggregates.CameraSensor;
import com.spottrack.platform.monitoring.domain.model.commands.RegisterCameraSensorCommand;
import com.spottrack.platform.shared.application.result.ApplicationError;
import com.spottrack.platform.shared.application.result.Result;

public interface CameraSensorCommandService {
    Result<CameraSensor, ApplicationError> handle(RegisterCameraSensorCommand command);
}
