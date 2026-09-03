package com.spottrack.platform.iam.application.internal.commandservices;

import com.spottrack.platform.iam.application.commandservices.PendingRegistrationCommandService;
import com.spottrack.platform.iam.application.internal.outboundservices.hashing.HashingService;
import com.spottrack.platform.iam.domain.model.aggregates.PendingRegistration;
import com.spottrack.platform.iam.domain.model.commands.SavePendingRegistrationCommand;
import com.spottrack.platform.iam.domain.repositories.PendingRegistrationRepository;
import com.spottrack.platform.iam.domain.repositories.UserRepository;
import com.spottrack.platform.shared.application.result.ApplicationError;
import com.spottrack.platform.shared.application.result.Result;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class PendingRegistrationCommandServiceImpl implements PendingRegistrationCommandService {

    private final PendingRegistrationRepository pendingRegistrationRepository;
    private final UserRepository userRepository;
    private final HashingService hashingService;

    public PendingRegistrationCommandServiceImpl(
            PendingRegistrationRepository pendingRegistrationRepository,
            UserRepository userRepository,
            HashingService hashingService) {
        this.pendingRegistrationRepository = pendingRegistrationRepository;
        this.userRepository = userRepository;
        this.hashingService = hashingService;
    }

    @Override
    public Result<UUID, ApplicationError> handle(SavePendingRegistrationCommand command) {
        try {
            if (userRepository.existsByUsername(command.email())) {
                return Result.failure(ApplicationError.conflict(
                        "PendingRegistration",
                        "An account already exists for email: " + command.email()
                ));
            }

            var existingPending = pendingRegistrationRepository.findByEmail(command.email());
            if (existingPending.isPresent() && existingPending.get().isPending() && !existingPending.get().isExpired()) {
                return Result.success(existingPending.get().getRegistrationId());
            }

            String hashedPassword = hashingService.encode(command.rawPassword());
            var pending = new PendingRegistration(hashedPassword, command);
            var saved = pendingRegistrationRepository.save(pending);
            return Result.success(saved.getRegistrationId());

        } catch (IllegalArgumentException e) {
            return Result.failure(ApplicationError.validationError("PendingRegistration", e.getMessage()));
        } catch (Exception e) {
            return Result.failure(ApplicationError.unexpected("PendingRegistration creation", e.getMessage()));
        }
    }
}
