package com.spottrack.platform.analytics.application.internal.eventhandlers;

import com.spottrack.platform.analytics.application.commandservices.UsageSessionCommandService;
import com.spottrack.platform.analytics.domain.model.commands.RecordUsageSessionCommand;
import com.spottrack.platform.monitoring.interfaces.events.SessionTimeCalculatedAnalyticsIntegrationEvent;
import com.spottrack.platform.shared.application.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Keeps every finished session on the timeline (see UsageSession). Sessions that
 * never captured motion carry no time window and add nothing to the history.
 */
@Component
@Slf4j
public class UsageSessionOnSessionTimeCalculatedEventHandler {
    private final UsageSessionCommandService usageSessionCommandService;

    public UsageSessionOnSessionTimeCalculatedEventHandler(UsageSessionCommandService usageSessionCommandService) {
        this.usageSessionCommandService = usageSessionCommandService;
    }

    @EventListener
    public void on(SessionTimeCalculatedAnalyticsIntegrationEvent event) {
        if (event.startedAt() == null || event.endedAt() == null) return;
        var result = usageSessionCommandService.handle(new RecordUsageSessionCommand(
                event.sessionTrackerId(), event.equipmentId(), event.startedAt(), event.endedAt()));
        if (result instanceof Result.Failure<?, ?> failure) {
            log.warn("Usage session for equipment {} not recorded: {}", event.equipmentId(), failure.error());
        }
    }
}
