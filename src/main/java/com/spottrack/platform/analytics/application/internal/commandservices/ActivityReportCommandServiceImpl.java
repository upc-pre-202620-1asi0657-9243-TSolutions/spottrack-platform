package com.spottrack.platform.analytics.application.internal.commandservices;

import com.spottrack.platform.analytics.application.commandservices.ActivityReportCommandService;
import com.spottrack.platform.analytics.domain.model.aggregates.ActivityReport;
import com.spottrack.platform.analytics.domain.model.commands.RequestActivityAnalysisCommand;
import com.spottrack.platform.analytics.domain.model.commands.RequestDowntimeCostCommand;
import com.spottrack.platform.analytics.domain.model.commands.RequestPercentageComparisonCommand;
import com.spottrack.platform.analytics.domain.model.commands.RequestTotalUsageTimeCommand;
import com.spottrack.platform.analytics.domain.model.valueobjects.ActivityReportId;
import com.spottrack.platform.analytics.domain.repositories.ActivityReportRepository;
import com.spottrack.platform.shared.application.result.ApplicationError;
import com.spottrack.platform.shared.application.result.Result;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class ActivityReportCommandServiceImpl implements ActivityReportCommandService {

    /**
     * Placeholder heuristic — there's no real cost-per-minute-of-downtime
     * business rule defined anywhere in the domain yet.
     */
    private static final double DOWNTIME_COST_PER_MINUTE = 2.0;

    private final ActivityReportRepository activityReportRepository;

    public ActivityReportCommandServiceImpl(ActivityReportRepository activityReportRepository) {
        this.activityReportRepository = activityReportRepository;
    }

    public Optional<ActivityReport> handle(RequestActivityAnalysisCommand command) {
        var activityReportId = new ActivityReportId(java.util.concurrent.ThreadLocalRandom.current().nextLong(1, Long.MAX_VALUE));
        var activityReport = new ActivityReport(command, activityReportId);

        activityReport.updateTotalUsageTime(command.minutesActive().longValue());
        activityReport.updateDowntimeCost(Math.round(command.minutesInactive() * DOWNTIME_COST_PER_MINUTE));
        activityReport.updatePercentageComparison(command.percentageChange());

        this.activityReportRepository.save(activityReport);

        return Optional.of(activityReport);
    }

    @Override
    public ActivityReport handle(RequestTotalUsageTimeCommand command) {
        var activityReport = activityReportRepository.findByEquipmentId(command.equipmentId())
                .orElseGet(() -> new ActivityReport(command.equipmentId()));
        activityReport.updateTotalUsageTime(activityReport.getTotalUsageTime() + command.minutesActive().longValue());
        return activityReportRepository.save(activityReport);
    }

    @Override
    public Result<ActivityReport, ApplicationError> handle(Long activityReportId, RequestDowntimeCostCommand command) {
        var found = activityReportRepository.findById(activityReportId);
        if (found.isEmpty()) {
            return Result.failure(ApplicationError.notFound("ActivityReport", activityReportId.toString()));
        }
        var activityReport = found.get();
        activityReport.updateDowntimeCost(Math.round(command.minutesInactive() * DOWNTIME_COST_PER_MINUTE));
        return Result.success(activityReportRepository.save(activityReport));
    }

    @Override
    public Result<ActivityReport, ApplicationError> handle(Long activityReportId, RequestPercentageComparisonCommand command) {
        var found = activityReportRepository.findById(activityReportId);
        if (found.isEmpty()) {
            return Result.failure(ApplicationError.notFound("ActivityReport", activityReportId.toString()));
        }
        var activityReport = found.get();
        activityReport.updatePercentageComparison(command.percentageChange());
        return Result.success(activityReportRepository.save(activityReport));
    }
}
