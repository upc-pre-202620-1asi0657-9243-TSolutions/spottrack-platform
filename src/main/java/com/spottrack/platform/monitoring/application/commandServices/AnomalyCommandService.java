package com.spottrack.platform.monitoring.application.commandServices;

import com.spottrack.platform.monitoring.domain.model.aggregates.Anomaly;
import com.spottrack.platform.monitoring.domain.model.commands.ReportAnomalyCommand;
import com.spottrack.platform.shared.application.result.ApplicationError;
import com.spottrack.platform.shared.application.result.Result;
import org.springframework.stereotype.Service;

@Service
public interface AnomalyCommandService {
    Result<Anomaly, ApplicationError> handle(ReportAnomalyCommand command);
}
