package com.spottrack.platform.iam.application.internal.commandservices;

import com.spottrack.platform.iam.application.commandservices.UserCommandService;
import com.spottrack.platform.iam.application.internal.outboundservices.hashing.HashingService;
import com.spottrack.platform.iam.application.internal.outboundservices.tokens.TokenService;
import com.spottrack.platform.iam.domain.model.aggregates.User;
import com.spottrack.platform.iam.domain.model.commands.ChangePasswordCommand;
import com.spottrack.platform.iam.domain.model.commands.DeactivateAccountCommand;
import com.spottrack.platform.iam.domain.model.commands.ForgotPasswordVerifyCommand;
import com.spottrack.platform.iam.domain.model.commands.ProvisionIamAccountCommand;
import com.spottrack.platform.iam.domain.model.commands.SignInCommand;
import com.spottrack.platform.profiles.interfaces.acl.ProfilesContextFacade;
import com.spottrack.platform.iam.domain.model.commands.SignOutCommand;
import com.spottrack.platform.iam.domain.model.commands.SignUpCommand;
import com.spottrack.platform.iam.domain.model.commands.UpdateNotificationPreferencesCommand;
import com.spottrack.platform.iam.domain.model.entities.Role;
import com.spottrack.platform.iam.domain.repositories.PendingRegistrationRepository;
import com.spottrack.platform.iam.domain.repositories.RoleRepository;
import com.spottrack.platform.iam.domain.repositories.UserRepository;
import com.spottrack.platform.iam.interfaces.events.RoleAssignedIntegrationEvent;
import com.spottrack.platform.shared.application.result.ApplicationError;
import com.spottrack.platform.shared.application.result.Result;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserCommandServiceImpl implements UserCommandService {

    private final UserRepository userRepository;
    private final HashingService hashingService;
    private final TokenService tokenService;
    private final RoleRepository roleRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final PendingRegistrationRepository pendingRegistrationRepository;
    private final ProfilesContextFacade profilesContextFacade;

    public UserCommandServiceImpl(
            UserRepository userRepository,
            HashingService hashingService,
            TokenService tokenService,
            RoleRepository roleRepository,
            ApplicationEventPublisher eventPublisher,
            PendingRegistrationRepository pendingRegistrationRepository,
            ProfilesContextFacade profilesContextFacade) {
        this.userRepository = userRepository;
        this.hashingService = hashingService;
        this.tokenService = tokenService;
        this.roleRepository = roleRepository;
        this.eventPublisher = eventPublisher;
        this.pendingRegistrationRepository = pendingRegistrationRepository;
        this.profilesContextFacade = profilesContextFacade;
    }

    @Override
    public Result<User, ApplicationError> handle(SignUpCommand command) {
        if (userRepository.existsByUsername(command.username())) {
            return Result.failure(ApplicationError.conflict("USER", "Username already exists: " + command.username()));
        }

        if (pendingRegistrationRepository.existsActivePendingByEmail(command.username())) {
            return Result.failure(ApplicationError.conflict(
                    "USER",
                    "A pending business registration exists for: " + command.username()
            ));
        }

        List<Role> roles = Role.validateRoleSet(command.roles()).stream()
                .map(role -> roleRepository.findByName(role.getName()).orElse(role))
                .toList();

        String encodedPassword = hashingService.encode(command.password());
        User user = new User(command.username(), encodedPassword, roles);
        User savedUser = userRepository.save(user);

        User reloadedUser = userRepository.findById(savedUser.getId()).orElse(savedUser);

        String firstRoleName = reloadedUser.getRoles().stream()
                .findFirst()
                .map(Role::getStringName)
                .orElse("");

        eventPublisher.publishEvent(new RoleAssignedIntegrationEvent(
                reloadedUser.getId(),
                reloadedUser.getUsername(),
                firstRoleName,
                "",
                "",
                "",
                ""
        ));

        return Result.success(reloadedUser);
    }

    @Override
    public Result<ImmutablePair<User, String>, ApplicationError> handle(SignInCommand command) {
        var userOptional = userRepository.findByUsername(command.username());
        if (userOptional.isEmpty()) {
            return Result.failure(ApplicationError.validationError("credentials", "Invalid credentials"));
        }
        var user = userOptional.get();
        if (!hashingService.matches(command.password(), user.getPassword())) {
            return Result.failure(ApplicationError.validationError("credentials", "Invalid credentials"));
        }
        if (!user.isActive()) {
            return Result.failure(ApplicationError.businessRuleViolation("account.deactivated", "This account has been deactivated"));
        }
        List<String> roleNames = user.getRoles().stream()
                .map(Role::getStringName)
                .toList();
        String token = tokenService.generateToken(user.getUsername(), roleNames);
        return Result.success(ImmutablePair.of(user, token));
    }

    @Override
    public Result<User, ApplicationError> handle(ChangePasswordCommand command) {
        var userOptional = userRepository.findByUsername(command.username());
        if (userOptional.isEmpty()) {
            return Result.failure(ApplicationError.notFound("USER", command.username()));
        }
        var user = userOptional.get();
        if (!hashingService.matches(command.currentPassword(), user.getPassword())) {
            return Result.failure(ApplicationError.validationError("password", "Current password is incorrect."));
        }
        user.setPassword(hashingService.encode(command.newPassword()));
        return Result.success(userRepository.save(user));
    }

    @Override
    public Result<User, ApplicationError> handle(UpdateNotificationPreferencesCommand command) {
        var userOptional = userRepository.findByUsername(command.username());
        if (userOptional.isEmpty()) {
            return Result.failure(ApplicationError.notFound("USER", command.username()));
        }
        var user = userOptional.get();
        user.setNotifyOnCritical(command.notifyOnCritical());
        user.setNotifyOnWarning(command.notifyOnWarning());
        user.setNotificationEmail(command.notificationEmail());
        return Result.success(userRepository.save(user));
    }

    @Override
    public Result<User, ApplicationError> handle(ForgotPasswordVerifyCommand command) {
        var genericError = ApplicationError.validationError("credentials",
                "Verification failed. The provided information does not match our records.");

        var userOptional = userRepository.findByUsername(command.email());
        if (userOptional.isEmpty()) {
            return Result.failure(genericError);
        }

        var storedDni = profilesContextFacade.fetchDniByEmail(command.email());
        if (storedDni.isBlank() || !storedDni.equals(command.dni())) {
            return Result.failure(genericError);
        }

        var user = userOptional.get();
        user.setPassword(hashingService.encode(command.newPassword()));
        return Result.success(userRepository.save(user));
    }

    @Override
    public Result<User, ApplicationError> handle(SignOutCommand command) {
        var userOptional = userRepository.findByUsername(command.username());
        if (userOptional.isEmpty()) {
            return Result.failure(ApplicationError.notFound("USER", command.username()));
        }
        return Result.success(userOptional.get());
    }

    @Override
    public Result<User, ApplicationError> handle(DeactivateAccountCommand command) {
        var userOptional = userRepository.findByUsername(command.username());
        if (userOptional.isEmpty()) {
            return Result.failure(ApplicationError.notFound("USER", command.username()));
        }
        var user = userOptional.get();
        user.deactivate();
        var savedUser = userRepository.save(user);
        return Result.success(savedUser);
    }

    @Override
    public Result<User, ApplicationError> handle(ProvisionIamAccountCommand command) {
        if (userRepository.existsByUsername(command.username())) {
            return Result.failure(ApplicationError.conflict("USER", "Username already exists: " + command.username()));
        }

        List<Role> roles = Role.validateRoleSet(command.roles()).stream()
                .map(role -> roleRepository.findByName(role.getName()).orElse(role))
                .toList();

        // alreadyHashedPassword is a BCrypt hash stored in pending_registrations.
        // It must NOT be re-encoded here — doing so would produce a hash-of-hash
        // that can never match the original password at sign-in time.
        User user = new User(command.username(), command.alreadyHashedPassword(), roles);
        User savedUser = userRepository.save(user);
        return Result.success(savedUser);
    }
}
