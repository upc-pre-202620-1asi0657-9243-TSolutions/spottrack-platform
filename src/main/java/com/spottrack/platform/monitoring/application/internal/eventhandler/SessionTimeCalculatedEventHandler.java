package com.spottrack.platform.monitoring.application.internal.eventhandler;

import com.spottrack.platform.monitoring.application.commandServices.SessionTrackerCommandService;
import com.spottrack.platform.monitoring.domain.model.commands.DeleteSessionTrackerCommand;
import com.spottrack.platform.monitoring.domain.model.events.SessionTimeCalculatedEvent;
import com.spottrack.platform.monitoring.interfaces.events.SessionTimeCalculatedAnalyticsIntegrationEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

/**
 * Implements the "(Session Time Calculated Successfully) Send Notification To
 * Analytics" policy from the event storming. Publishing the integration event
 * below is where this context's responsibility for the *data* ends — recording/
 * aggregating it is not this bounded context's job. Whoever owns that
 * responsibility should add a listener for
 * {@link SessionTimeCalculatedAnalyticsIntegrationEvent} (e.g. in the Analytics
 * bounded context) and implement the real ingestion there.
 *
 * Once its final activity has been reported, the session tracker itself has
 * nothing left to do, so it's deleted here.
 */
@Service
@Slf4j
public class SessionTimeCalculatedEventHandler {
    private final ApplicationEventPublisher eventPublisher;
    private final SessionTrackerCommandService sessionTrackerCommandService;

    public SessionTimeCalculatedEventHandler(ApplicationEventPublisher eventPublisher, SessionTrackerCommandService sessionTrackerCommandService) {
        this.eventPublisher = eventPublisher;
        this.sessionTrackerCommandService = sessionTrackerCommandService;
    }

    @EventListener
    public void on(SessionTimeCalculatedEvent event) {
        log.info("Session time calculated for session tracker {}; publishing integration event for Analytics.",
                event.sessionTrackerId().uuid());
        eventPublisher.publishEvent(new SessionTimeCalculatedAnalyticsIntegrationEvent(
                event.sessionTrackerId().uuid(),
                event.equipmentId().uuid(),
                event.trueActivity()
        ));
        sessionTrackerCommandService.handle(new DeleteSessionTrackerCommand(event.sessionTrackerId()));
    }
}
