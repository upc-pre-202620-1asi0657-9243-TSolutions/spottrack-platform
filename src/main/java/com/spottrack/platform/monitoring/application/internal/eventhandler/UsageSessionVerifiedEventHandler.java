package com.spottrack.platform.monitoring.application.internal.eventhandler;

import com.spottrack.platform.monitoring.application.commandServices.SessionTrackerCommandService;
import com.spottrack.platform.monitoring.application.queryServices.SessionTrackerQueryService;
import com.spottrack.platform.monitoring.domain.model.commands.EndUsageSessionCommand;
import com.spottrack.platform.monitoring.domain.model.events.UsageSessionVerifiedEvent;
import com.spottrack.platform.monitoring.domain.model.queries.GetSessionTrackerByIdQuery;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Service
public class UsageSessionVerifiedEventHandler {
    SessionTrackerCommandService sessionTrackerCommandService;
    SessionTrackerQueryService sessionTrackerQueryService;

    public UsageSessionVerifiedEventHandler(SessionTrackerCommandService sessionTrackerCommandService, SessionTrackerQueryService sessionTrackerQueryService){
        this.sessionTrackerCommandService =  sessionTrackerCommandService;
        this.sessionTrackerQueryService = sessionTrackerQueryService;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(UsageSessionVerifiedEvent event){
        /**
         * This event handler needs to execute the command of end session if the inactivity of a session fulfill specific conditions
         */
        var query = new GetSessionTrackerByIdQuery(event.sessionTrackerId());
        var session = sessionTrackerQueryService.handle(query);
        var inactive = session.get().isSessionIsInactive();

        if (inactive){
            var command = new EndUsageSessionCommand(event.sessionTrackerId());
            sessionTrackerCommandService.handle(command);
        }
    }


}
