package com.spottrack.platform.monitoring.application.internal.eventhandler;

import com.spottrack.platform.monitoring.application.commandServices.SessionTrackerCommandService;
import com.spottrack.platform.monitoring.domain.model.commands.CalculateSessionTimeCommand;
import com.spottrack.platform.monitoring.domain.model.events.UsageSessionEndedEvent;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Service
public class UsageSessionEndedEventHandler {
    private final SessionTrackerCommandService sessionTrackerCommandService;

    public UsageSessionEndedEventHandler(SessionTrackerCommandService sessionTrackerCommandService) {
        this.sessionTrackerCommandService = sessionTrackerCommandService;
    }

    /**
     * When a session ends, it's about to be reported on, so its true active time
     * is calculated right away.
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(UsageSessionEndedEvent event) {
        var command = new CalculateSessionTimeCommand(event.sessionTrackerId());
        sessionTrackerCommandService.handle(command);
    }
}
