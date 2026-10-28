package com.spottrack.platform.iam.interfaces.rest;

import com.spottrack.platform.iam.application.commandservices.UserCommandService;
import com.spottrack.platform.iam.application.queryservices.UserQueryService;
import com.spottrack.platform.iam.domain.model.aggregates.User;
import com.spottrack.platform.iam.domain.model.commands.RegisterTechnicianCommand;
import com.spottrack.platform.iam.domain.model.entities.Role;
import com.spottrack.platform.iam.domain.model.queries.GetUserByUsernameQuery;
import com.spottrack.platform.iam.domain.model.valueobjects.Roles;
import com.spottrack.platform.iam.interfaces.rest.resources.RegisterTechnicianResource;
import com.spottrack.platform.iam.interfaces.rest.resources.TechnicianAccountResource;
import com.spottrack.platform.shared.application.result.ApplicationError;
import com.spottrack.platform.shared.application.result.Result;
import com.spottrack.platform.shared.interfaces.rest.resources.ErrorResource;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UsersControllerRegisterTechnicianTest {

    private static final String ADMIN_EMAIL = "admin@gym.com";
    private static final Long ADMIN_USER_ID = 42L;

    private UserCommandService commandService;
    private UserQueryService queryService;
    private UsersController controller;
    private UsernamePasswordAuthenticationToken adminAuth;

    @BeforeEach
    void setUp() {
        commandService = mock(UserCommandService.class);
        queryService = mock(UserQueryService.class);
        controller = new UsersController(commandService, queryService);
        adminAuth = new UsernamePasswordAuthenticationToken(ADMIN_EMAIL, "n/a",
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
        var admin = new User(ADMIN_EMAIL, "hash", List.of(new Role(Roles.ROLE_ADMIN)));
        admin.setId(ADMIN_USER_ID);
        when(queryService.handle(new GetUserByUsernameQuery(ADMIN_EMAIL))).thenReturn(Optional.of(admin));
    }

    private static RegisterTechnicianResource resource() {
        return new RegisterTechnicianResource(" tecnico@gym.com ", "secret123", " Maria ", "Perez ");
    }

    @Test
    void returns201WithIdEmailRolesAndTechnicianId() {
        var technician = new User("tecnico@gym.com", "hash", List.of(new Role(Roles.ROLE_TECHNICIAN)));
        technician.setId(10L);
        when(commandService.handle(any(RegisterTechnicianCommand.class)))
                .thenReturn(Result.success(ImmutablePair.of(technician, "tech-1")));

        var response = controller.registerTechnician(resource(), adminAuth);

        assertEquals(201, response.getStatusCode().value());
        assertEquals(new TechnicianAccountResource(10L, "tecnico@gym.com", List.of("ROLE_TECHNICIAN"), "tech-1"),
                response.getBody());
    }

    @Test
    void passesTrimmedDataAndTheCallerAsOwningAdmin() {
        when(commandService.handle(any(RegisterTechnicianCommand.class)))
                .thenReturn(Result.failure(ApplicationError.conflict("USER", "Username already exists")));

        controller.registerTechnician(resource(), adminAuth);

        var captor = ArgumentCaptor.forClass(RegisterTechnicianCommand.class);
        verify(commandService).handle(captor.capture());
        assertEquals(new RegisterTechnicianCommand("tecnico@gym.com", "secret123", "Maria", "Perez", ADMIN_USER_ID),
                captor.getValue());
    }

    @Test
    void duplicateEmailIsMappedTo409UserConflict() {
        when(commandService.handle(any(RegisterTechnicianCommand.class)))
                .thenReturn(Result.failure(ApplicationError.conflict("USER", "Username already exists: x")));

        var response = controller.registerTechnician(resource(), adminAuth);

        assertEquals(409, response.getStatusCode().value());
        assertEquals("USER_CONFLICT", assertInstanceOf(ErrorResource.class, response.getBody()).code());
    }

    @Test
    void unknownCallerIsNotFoundAndNothingIsCreated() {
        when(queryService.handle(new GetUserByUsernameQuery(ADMIN_EMAIL))).thenReturn(Optional.empty());

        var response = controller.registerTechnician(resource(), adminAuth);

        assertEquals(404, response.getStatusCode().value());
        verify(commandService, never()).handle(any(RegisterTechnicianCommand.class));
    }

    @Test
    void endpointIsRestrictedToAdministrators() throws NoSuchMethodException {
        var method = UsersController.class.getMethod("registerTechnician",
                RegisterTechnicianResource.class, org.springframework.security.core.Authentication.class);

        assertEquals("hasRole('ADMIN')", method.getAnnotation(PreAuthorize.class).value());
    }
}
