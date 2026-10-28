package com.spottrack.platform.shared.application.commandservices;

import com.spottrack.platform.shared.application.result.ApplicationError;
import com.spottrack.platform.shared.application.result.Result;
import com.spottrack.platform.shared.domain.model.aggregates.Alert;
import com.spottrack.platform.shared.domain.model.commands.ClearAlertCommand;
import com.spottrack.platform.shared.domain.model.commands.ClearResolvedAlertsCommand;
import com.spottrack.platform.shared.domain.model.commands.CreateAlertCommand;
import com.spottrack.platform.shared.domain.model.commands.ResolveAlertCommand;

public interface AlertCommandService {
    Result<Alert, ApplicationError> handle(CreateAlertCommand command);
    Result<Alert, ApplicationError> handle(ResolveAlertCommand command);
    Result<Alert, ApplicationError> handle(ClearAlertCommand command);

    /** Clears every resolved alert of the admin and returns how many were cleared by this call. */
    Result<Integer, ApplicationError> handle(ClearResolvedAlertsCommand command);
}
