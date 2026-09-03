package com.spottrack.platform.monitoring.application.commandServices;

import com.spottrack.platform.monitoring.domain.model.aggregates.SessionTracker;
import com.spottrack.platform.monitoring.domain.model.commands.*;
import com.spottrack.platform.shared.application.result.ApplicationError;
import com.spottrack.platform.shared.application.result.Result;

public interface SessionTrackerCommandService {
    Result<SessionTracker, ApplicationError> handle(VerifyUsageSessionCommand command);
    Result<SessionTracker, ApplicationError> handle(CreateSessionTrackerCommand command);
    Result<SessionTracker, ApplicationError> handle(MotionSensorCaptureCommand command);
    Result<SessionTracker, ApplicationError> handle(CameraCaptureMotionCommand command);
    Result<SessionTracker, ApplicationError> handle(EndUsageSessionCommand command);
    Result<SessionTracker, ApplicationError> handle(CalculateSessionTimeCommand command);
    Result<Void, ApplicationError> handle(DeleteSessionTrackerCommand command);
}
