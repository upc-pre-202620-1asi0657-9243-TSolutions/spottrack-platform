package com.spottrack.platform.analytics.application.commandservices;

import com.spottrack.platform.analytics.domain.model.aggregates.ActivityReport;
import com.spottrack.platform.analytics.domain.model.commands.RequestActivityAnalysisCommand;
import com.spottrack.platform.analytics.domain.model.commands.RequestDowntimeCostCommand;
import com.spottrack.platform.analytics.domain.model.commands.RequestPercentageComparisonCommand;
import com.spottrack.platform.analytics.domain.model.commands.RequestTotalUsageTimeCommand;
import com.spottrack.platform.shared.application.result.ApplicationError;
import com.spottrack.platform.shared.application.result.Result;

import java.util.Optional;

public interface ActivityReportCommandService {
    Optional<ActivityReport> handle(RequestActivityAnalysisCommand command);

    /**
     * Finds-or-creates the ActivityReport for command.equipmentId() and
     * accumulates minutesActive onto its totalUsageTime.
     */
    ActivityReport handle(RequestTotalUsageTimeCommand command);

    Result<ActivityReport, ApplicationError> handle(Long activityReportId, RequestDowntimeCostCommand command);

    Result<ActivityReport, ApplicationError> handle(Long activityReportId, RequestPercentageComparisonCommand command);
}
