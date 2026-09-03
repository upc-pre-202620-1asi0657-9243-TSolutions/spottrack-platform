package com.spottrack.platform.monitoring.application.commandServices;

import com.spottrack.platform.monitoring.domain.model.aggregates.MotionSensor;
import com.spottrack.platform.monitoring.domain.model.commands.MarkMotionSensorDisconnectedCommand;
import com.spottrack.platform.monitoring.domain.model.commands.MarkMotionSensorReconnectedCommand;
import com.spottrack.platform.monitoring.domain.model.commands.RegisterMotionSensorCommand;
import com.spottrack.platform.shared.application.result.ApplicationError;
import com.spottrack.platform.shared.application.result.Result;

public interface MotionSensorCommandService {
    Result<MotionSensor, ApplicationError> handle(RegisterMotionSensorCommand command);
    Result<MotionSensor, ApplicationError> handle(MarkMotionSensorDisconnectedCommand command);
    Result<MotionSensor, ApplicationError> handle(MarkMotionSensorReconnectedCommand command);
}
