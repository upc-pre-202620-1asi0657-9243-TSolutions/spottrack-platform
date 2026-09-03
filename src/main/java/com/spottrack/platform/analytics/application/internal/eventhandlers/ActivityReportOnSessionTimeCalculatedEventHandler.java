package com.spottrack.platform.analytics.application.internal.eventhandlers;

import com.spottrack.platform.analytics.application.commandservices.ActivityReportCommandService;
import com.spottrack.platform.analytics.domain.model.commands.RequestTotalUsageTimeCommand;
import com.spottrack.platform.monitoring.interfaces.events.SessionTimeCalculatedAnalyticsIntegrationEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Ingests the integration event monitoring publishes once a session's true
 * activity is known, feeding it into RequestTotalUsageTimeCommand so real
 * usage data accumulates onto ActivityReport instead of disappearing along
 * with the deleted session tracker.
 */
@Component
public class ActivityReportOnSessionTimeCalculatedEventHandler {
    private final ActivityReportCommandService activityReportCommandService;

    public ActivityReportOnSessionTimeCalculatedEventHandler(ActivityReportCommandService activityReportCommandService) {
        this.activityReportCommandService = activityReportCommandService;
    }

    @EventListener
    public void on(SessionTimeCalculatedAnalyticsIntegrationEvent event) {
        var minutesActive = (int) Math.round(event.trueActivity().toSecondOfDay() / 60.0);
        activityReportCommandService.handle(new RequestTotalUsageTimeCommand(event.equipmentId(), minutesActive));
    }
}
