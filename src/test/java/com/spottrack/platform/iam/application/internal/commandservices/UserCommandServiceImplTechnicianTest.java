package com.spottrack.platform.iam.application.internal.commandservices;

import com.spottrack.platform.iam.application.internal.outboundservices.hashing.HashingService;
import com.spottrack.platform.iam.application.internal.outboundservices.tokens.TokenService;
import com.spottrack.platform.iam.domain.model.aggregates.User;
import com.spottrack.platform.iam.domain.model.commands.RegisterTechnicianCommand;
import com.spottrack.platform.iam.domain.model.commands.SignUpCommand;
import com.spottrack.platform.iam.domain.model.entities.Role;
import com.spottrack.platform.iam.domain.model.valueobjects.Roles;
import com.spottrack.platform.iam.domain.repositories.PendingRegistrationRepository;
import com.spottrack.platform.iam.domain.repositories.RoleRepository;
import com.spottrack.platform.iam.domain.repositories.UserRepository;
import com.spottrack.platform.maintenance.interfaces.acl.MaintenanceContextFacade;
import com.spottrack.platform.profiles.interfaces.acl.ProfilesContextFacade;
import com.spottrack.platform.shared.application.result.ApplicationError;
import com.spottrack.platform.shared.application.result.Result;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserCommandServiceImplTechnicianTest {

    private static final String EMAIL = "tecnico@gym.com";
    private static final Long ADMIN_USER_ID = 42L;

    private UserRepository userRepository;
    private HashingService hashingService;
    private RoleRepository roleRepository;
    private PendingRegistrationRepository pendingRegistrationRepository;
    private MaintenanceContextFacade maintenanceContextFacade;
    private ApplicationEventPublisher eventPublisher;
    private UserCommandServiceImpl service;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        hashingService = mock(HashingService.class);
        roleRepository = mock(RoleRepository.class);
        pendingRegistrationRepository = mock(PendingRegistrationRepository.class);
        maintenanceContextFacade = mock(MaintenanceContextFacade.class);
        eventPublisher = mock(ApplicationEventPublisher.class);
        service = new UserCommandServiceImpl(userRepository, hashingService, mock(TokenService.class),
                roleRepository, eventPublisher, pendingRegistrationRepository, mock(ProfilesContextFacade.class),
                maintenanceContextFacade);

        when(hashingService.encode("secret123")).thenReturn("hashed");
        when(roleRepository.findByName(Roles.ROLE_TECHNICIAN))
                .thenReturn(Optional.of(new Role(3L, Roles.ROLE_TECHNICIAN)));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(10L);
            return user;
        });
    }

    private static RegisterTechnicianCommand command() {
        return new RegisterTechnicianCommand(EMAIL, "secret123", "Maria", "Perez", ADMIN_USER_ID);
    }

    @Test
    void createsAccountWithTechnicianRoleOnlyAndHashedPassword() {
        when(maintenanceContextFacade.provisionTechnician(anyLong(), anyLong(), anyString()))
                .thenReturn(Result.success("tech-1"));

        var result = service.handle(command());

        var pair = valueOf(result);
        assertEquals("tech-1", pair.getRight());
        assertEquals(EMAIL, pair.getLeft().getUsername());
        assertEquals("hashed", pair.getLeft().getPassword());
        assertEquals(List.of(Roles.ROLE_TECHNICIAN),
                pair.getLeft().getRoles().stream().map(Role::getName).toList());
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void registersRosterEntryForTheCallerAdminWithFullName() {
        when(maintenanceContextFacade.provisionTechnician(anyLong(), anyLong(), anyString()))
                .thenReturn(Result.success("tech-1"));

        service.handle(command());

        verify(maintenanceContextFacade).provisionTechnician(10L, ADMIN_USER_ID, "Maria Perez");
    }

    @Test
    void duplicateEmailIsAConflictAndNothingIsSaved() {
        when(userRepository.existsByUsername(EMAIL)).thenReturn(true);

        var error = errorOf(service.handle(command()));

        assertEquals("USER_CONFLICT", error.code());
        verify(userRepository, never()).save(any());
        verify(maintenanceContextFacade, never()).provisionTechnician(anyLong(), anyLong(), anyString());
    }

    @Test
    void pendingBusinessRegistrationWithSameEmailIsAConflict() {
        when(pendingRegistrationRepository.existsActivePendingByEmail(EMAIL)).thenReturn(true);

        var error = errorOf(service.handle(command()));

        assertEquals("USER_CONFLICT", error.code());
        verify(userRepository, never()).save(any());
    }

    @Test
    void rosterFailureMarksTransactionRollbackOnlyAndReturnsTheError() {
        var rosterError = ApplicationError.unexpected("Technician creation", "db down");
        when(maintenanceContextFacade.provisionTechnician(anyLong(), anyLong(), anyString()))
                .thenReturn(Result.failure(rosterError));
        var status = mock(TransactionStatus.class);

        try (MockedStatic<TransactionAspectSupport> support = mockStatic(TransactionAspectSupport.class)) {
            support.when(TransactionAspectSupport::currentTransactionStatus).thenReturn(status);

            var error = errorOf(service.handle(command()));

            assertEquals(rosterError, error);
            verify(status).setRollbackOnly();
        }
    }

    @Test
    void rosterFailureOutsideTransactionStillReturnsTheError() {
        var rosterError = ApplicationError.unexpected("Technician creation", "db down");
        when(maintenanceContextFacade.provisionTechnician(anyLong(), anyLong(), anyString()))
                .thenReturn(Result.failure(rosterError));

        assertEquals(rosterError, errorOf(service.handle(command())));
    }

    @Test
    void signUpRejectsTechnicianRoleWith422AndDoesNotSaveAnything() {
        var command = new SignUpCommand("x@gym.com", "secret123", List.of(new Role(Roles.ROLE_TECHNICIAN)));

        var error = errorOf(service.handle(command));

        assertEquals("BUSINESS_RULE_VIOLATION", error.code());
        assertTrue(error.details().contains("Technician accounts"));
        verify(userRepository, never()).save(any());
        verify(userRepository, never()).existsByUsername(eq("x@gym.com"));
    }

    @Test
    void signUpRejectsTechnicianRoleEvenWhenMixedWithOthers() {
        var command = new SignUpCommand("x@gym.com", "secret123",
                List.of(new Role(Roles.ROLE_ADMIN), new Role(Roles.ROLE_TECHNICIAN)));

        assertEquals("BUSINESS_RULE_VIOLATION", errorOf(service.handle(command)).code());
    }

    @Test
    void signUpStillAcceptsClientAndAdminRoles() {
        when(roleRepository.findByName(Roles.ROLE_CLIENT)).thenReturn(Optional.empty());
        when(userRepository.findById(10L)).thenReturn(Optional.empty());

        var user = valueOf(service.handle(new SignUpCommand("c@gym.com", "secret123", List.of())));

        assertEquals(List.of(Roles.ROLE_CLIENT), user.getRoles().stream().map(Role::getName).toList());
    }

    @SuppressWarnings("unchecked")
    private static <T> T valueOf(Result<T, ApplicationError> result) {
        return ((Result.Success<T, ApplicationError>) assertInstanceOf(Result.Success.class, result)).value();
    }

    private static ApplicationError errorOf(Result<?, ApplicationError> result) {
        return ((Result.Failure<?, ApplicationError>) assertInstanceOf(Result.Failure.class, result)).error();
    }
}
