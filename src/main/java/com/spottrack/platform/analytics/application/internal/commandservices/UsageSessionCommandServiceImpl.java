package com.spottrack.platform.analytics.application.internal.commandservices;

import com.spottrack.platform.analytics.application.commandservices.UsageSessionCommandService;
import com.spottrack.platform.analytics.domain.model.aggregates.UsageSession;
import com.spottrack.platform.analytics.domain.model.commands.RecordUsageSessionCommand;
import com.spottrack.platform.analytics.domain.repositories.UsageSessionRepository;
import com.spottrack.platform.shared.application.result.ApplicationError;
import com.spottrack.platform.shared.application.result.Result;
import org.springframework.stereotype.Service;

@Service
public class UsageSessionCommandServiceImpl implements UsageSessionCommandService {

    private final UsageSessionRepository usageSessionRepository;

    public UsageSessionCommandServiceImpl(UsageSessionRepository usageSessionRepository) {
        this.usageSessionRepository = usageSessionRepository;
    }

    @Override
    public Result<UsageSession, ApplicationError> handle(RecordUsageSessionCommand command) {
        try {
            var usageSession = new UsageSession(command.sessionTrackerId(), command.equipmentId(),
                    command.startedAt(), command.endedAt());
            return Result.success(usageSessionRepository.save(usageSession));
        } catch (IllegalArgumentException e) {
            return Result.failure(ApplicationError.validationError("usageSession", e.getMessage()));
        }
    }
}
