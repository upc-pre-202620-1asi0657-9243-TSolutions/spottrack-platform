package com.spottrack.platform.monitoring.application.commandServices;

import com.spottrack.platform.monitoring.domain.model.aggregates.SensorPairing;
import com.spottrack.platform.monitoring.domain.model.commands.ExpireDueSensorPairingsCommand;
import com.spottrack.platform.monitoring.domain.model.commands.IssueSensorPairingCommand;
import com.spottrack.platform.monitoring.domain.model.commands.MarkSensorPairingUsedCommand;
import com.spottrack.platform.monitoring.domain.model.valueobjects.IssuedSensorPairing;
import com.spottrack.platform.shared.application.result.ApplicationError;
import com.spottrack.platform.shared.application.result.Result;

public interface SensorPairingCommandService {
    Result<IssuedSensorPairing, ApplicationError> handle(IssueSensorPairingCommand command);

    Result<SensorPairing, ApplicationError> handle(MarkSensorPairingUsedCommand command);

    /** Expires the pending pairings past their TTL; the result is how many were expired. */
    Result<Integer, ApplicationError> handle(ExpireDueSensorPairingsCommand command);
}
