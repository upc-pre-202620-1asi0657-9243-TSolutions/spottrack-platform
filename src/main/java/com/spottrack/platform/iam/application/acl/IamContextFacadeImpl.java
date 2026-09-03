package com.spottrack.platform.iam.application.acl;

import com.spottrack.platform.iam.application.commandservices.PendingRegistrationCommandService;
import com.spottrack.platform.iam.application.commandservices.UserCommandService;
import com.spottrack.platform.iam.application.queryservices.UserQueryService;
import com.spottrack.platform.iam.domain.model.aggregates.PendingRegistration;
import com.spottrack.platform.iam.domain.model.commands.ProvisionIamAccountCommand;
import com.spottrack.platform.iam.domain.model.commands.SavePendingRegistrationCommand;
import com.spottrack.platform.iam.domain.model.entities.Role;
import com.spottrack.platform.iam.domain.model.queries.GetUserByUsernameQuery;
import com.spottrack.platform.iam.domain.model.valueobjects.Roles;
import com.spottrack.platform.iam.domain.repositories.PendingRegistrationRepository;
import com.spottrack.platform.iam.interfaces.acl.IamContextFacade;
import com.spottrack.platform.shared.application.result.ApplicationError;
import com.spottrack.platform.shared.application.result.Result;
import com.spottrack.platform.shared.domain.model.valueobjects.AlertSeverity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
public class IamContextFacadeImpl implements IamContextFacade {

    private final UserQueryService userQueryService;
    private final UserCommandService userCommandService;
    private final PendingRegistrationCommandService pendingRegistrationCommandService;
    private final PendingRegistrationRepository pendingRegistrationRepository;

    public IamContextFacadeImpl(
            UserQueryService userQueryService,
            UserCommandService userCommandService,
            PendingRegistrationCommandService pendingRegistrationCommandService,
            PendingRegistrationRepository pendingRegistrationRepository) {
        this.userQueryService = userQueryService;
        this.userCommandService = userCommandService;
        this.pendingRegistrationCommandService = pendingRegistrationCommandService;
        this.pendingRegistrationRepository = pendingRegistrationRepository;
    }

    @Override
    public Optional<Long> fetchUserIdByUsername(String username) {
        return userQueryService.handle(new GetUserByUsernameQuery(username))
                .map(user -> user.getId());
    }

    @Override
    public boolean existsUserByUsername(String username) {
        return userQueryService.handle(new GetUserByUsernameQuery(username)).isPresent();
    }

    @Override
    public boolean existsUserById(Long userId) {
        return userQueryService.handle(new com.spottrack.platform.iam.domain.model.queries.GetUserByIdQuery(userId)).isPresent();
    }

    @Override
    public boolean shouldNotify(Long userId, AlertSeverity severity) {
        return userQueryService.handle(new com.spottrack.platform.iam.domain.model.queries.GetUserByIdQuery(userId))
                .map(user -> severity == AlertSeverity.CRITICAL ? user.isNotifyOnCritical() : user.isNotifyOnWarning())
                .orElse(true);
    }

    @Override
    public Result<UUID, ApplicationError> savePendingRegistration(SavePendingRegistrationCommand command) {
        return pendingRegistrationCommandService.handle(command);
    }

    @Override
    public boolean existsPendingRegistrationByEmail(String email) {
        return pendingRegistrationRepository.existsActivePendingByEmail(email);
    }

    @Override
    @Transactional
    public Long consumePendingRegistration(UUID registrationId) {
        var pendingOpt = pendingRegistrationRepository.findById(registrationId);
        if (pendingOpt.isEmpty()) {
            log.warn("consumePendingRegistration: no pending registration found for id {}", registrationId);
            return 0L;
        }

        var pending = pendingOpt.get();

        if (pending.getStatus().name().equals("CONSUMED")) {
            log.info("consumePendingRegistration: registration {} already consumed, returning existing user", registrationId);
            var existingUser = userQueryService.handle(new GetUserByUsernameQuery(pending.getEmail()));
            if (existingUser.isEmpty()) {
                log.error("consumePendingRegistration: registration {} is CONSUMED but no IAM user found for email {}",
                        registrationId, pending.getEmail());
                return 0L;
            }
            return existingUser.get().getId();
        }

        if (pending.isExpired()) {
            log.warn("consumePendingRegistration: registration {} has expired", registrationId);
            return 0L;
        }

        var existingUser = userQueryService.handle(new GetUserByUsernameQuery(pending.getEmail()));
        if (existingUser.isPresent()) {
            log.warn("consumePendingRegistration: IAM user already exists for {} — marking pending {} as consumed",
                    pending.getEmail(), registrationId);
            pending.consume();
            pendingRegistrationRepository.save(pending);
            return existingUser.get().getId();
        }

        var provisionCommand = new ProvisionIamAccountCommand(
                pending.getEmail(),
                pending.getHashedPassword(),
                List.of(new Role(Roles.ROLE_ADMIN))
        );

        var result = userCommandService.handle(provisionCommand);
        if (result instanceof Result.Failure<?, ?> failure) {
            log.error("consumePendingRegistration: failed to provision IAM account for registration {}: {}",
                    registrationId, failure.error());
            return 0L;
        }

        var newUser = ((Result.Success<com.spottrack.platform.iam.domain.model.aggregates.User, ?>) result).value();
        pending.consume();
        pendingRegistrationRepository.save(pending);

        log.info("consumePendingRegistration: provisioned IAM user {} for registration {}", newUser.getId(), registrationId);
        return newUser.getId();
    }

    @Override
    public String fetchPendingRegistrationEmail(UUID registrationId) {
        return pendingRegistrationRepository.findById(registrationId)
                .map(PendingRegistration::getEmail)
                .orElse("");
    }

    @Override
    public String fetchPendingRegistrationCompanyName(UUID registrationId) {
        return pendingRegistrationRepository.findById(registrationId)
                .map(PendingRegistration::getCompanyName)
                .orElse("");
    }

    @Override
    public String fetchPendingRegistrationRuc(UUID registrationId) {
        return pendingRegistrationRepository.findById(registrationId)
                .map(PendingRegistration::getRuc)
                .orElse("");
    }

    @Override
    public String fetchPendingRegistrationLegalStructure(UUID registrationId) {
        return pendingRegistrationRepository.findById(registrationId)
                .map(PendingRegistration::getLegalStructure)
                .orElse("");
    }

    @Override
    public String fetchPendingRegistrationCompanyPhone(UUID registrationId) {
        return pendingRegistrationRepository.findById(registrationId)
                .map(PendingRegistration::getCompanyPhone)
                .orElse("");
    }

    @Override
    public String fetchPendingRegistrationCompanyEmail(UUID registrationId) {
        return pendingRegistrationRepository.findById(registrationId)
                .map(PendingRegistration::getCompanyEmail)
                .orElse("");
    }

    @Override
    public String fetchPendingRegistrationStreetAddress(UUID registrationId) {
        return pendingRegistrationRepository.findById(registrationId)
                .map(PendingRegistration::getStreetAddress)
                .orElse("");
    }

    @Override
    public String fetchPendingRegistrationCity(UUID registrationId) {
        return pendingRegistrationRepository.findById(registrationId)
                .map(PendingRegistration::getCity)
                .orElse("");
    }

    @Override
    public String fetchPendingRegistrationDistrict(UUID registrationId) {
        return pendingRegistrationRepository.findById(registrationId)
                .map(PendingRegistration::getDistrict)
                .orElse("");
    }

    @Override
    public String fetchPendingRegistrationFirstName(UUID registrationId) {
        return pendingRegistrationRepository.findById(registrationId)
                .map(PendingRegistration::getFirstName)
                .orElse("");
    }

    @Override
    public String fetchPendingRegistrationLastName(UUID registrationId) {
        return pendingRegistrationRepository.findById(registrationId)
                .map(PendingRegistration::getLastName)
                .orElse("");
    }

    @Override
    public String fetchPendingRegistrationPhoneNumber(UUID registrationId) {
        return pendingRegistrationRepository.findById(registrationId)
                .map(PendingRegistration::getPhoneNumber)
                .orElse("");
    }

    @Override
    public String fetchPendingRegistrationDni(UUID registrationId) {
        return pendingRegistrationRepository.findById(registrationId)
                .map(PendingRegistration::getDni)
                .orElse("");
    }
}
