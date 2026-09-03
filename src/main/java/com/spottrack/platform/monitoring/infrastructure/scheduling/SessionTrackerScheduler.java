package com.spottrack.platform.monitoring.infrastructure.scheduling;

import com.spottrack.platform.monitoring.application.commandServices.SessionTrackerCommandService;
import com.spottrack.platform.monitoring.domain.model.commands.VerifyUsageSessionCommand;
import com.spottrack.platform.monitoring.domain.model.valueobjects.SessionTrackerId;
import com.spottrack.platform.monitoring.infrastructure.persistence.jpa.repositories.SessionTrackerPersistenceRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SessionTrackerScheduler {
    private final SessionTrackerPersistenceRepository repository;
    private final SessionTrackerCommandService commandService;

    public SessionTrackerScheduler(SessionTrackerPersistenceRepository repository, SessionTrackerCommandService commandService) {
        this.repository = repository;
        this.commandService = commandService;
    }


    @Scheduled(fixedRate = 60000) // Runs every 60 seconds
    public void verifyActiveSessions() {
        var activeSessions = repository.findAllBySessionIsActiveTrue();
        for (var session : activeSessions) {
            var command = new VerifyUsageSessionCommand(new SessionTrackerId(session.getSessionTrackerId()));
            commandService.handle(command);
        }
    }

}
