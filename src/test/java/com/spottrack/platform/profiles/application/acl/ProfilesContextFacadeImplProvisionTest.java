package com.spottrack.platform.profiles.application.acl;

import com.spottrack.platform.gym.interfaces.acl.GymContextFacade;
import com.spottrack.platform.profiles.application.commandservices.AdminCommandService;
import com.spottrack.platform.profiles.application.commandservices.BusinessProfileCommandService;
import com.spottrack.platform.profiles.application.queryservices.AdminQueryService;
import com.spottrack.platform.profiles.application.queryservices.ClientQueryService;
import com.spottrack.platform.profiles.domain.model.aggregates.Admin;
import com.spottrack.platform.profiles.domain.model.commands.CreateAdminCommand;
import com.spottrack.platform.profiles.domain.model.commands.CreateBusinessProfileCommand;
import com.spottrack.platform.profiles.domain.model.queries.GetAdminByUserIdQuery;
import com.spottrack.platform.profiles.infrastructure.persistence.jpa.repositories.ClientGymAssociationPersistenceRepository;
import com.spottrack.platform.shared.application.result.ApplicationError;
import com.spottrack.platform.shared.application.result.Result;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ProfilesContextFacadeImplProvisionTest {

    private static final Long USER_ID = 42L;

    private AdminQueryService adminQueryService;
    private AdminCommandService adminCommandService;
    private BusinessProfileCommandService businessProfileCommandService;
    private ProfilesContextFacadeImpl facade;

    @BeforeEach
    void setUp() {
        adminQueryService = mock(AdminQueryService.class);
        adminCommandService = mock(AdminCommandService.class);
        businessProfileCommandService = mock(BusinessProfileCommandService.class);
        facade = new ProfilesContextFacadeImpl(
                mock(ClientQueryService.class),
                adminQueryService,
                adminCommandService,
                businessProfileCommandService,
                mock(ClientGymAssociationPersistenceRepository.class),
                mock(GymContextFacade.class));
        when(adminQueryService.handle(any(GetAdminByUserIdQuery.class))).thenReturn(Optional.empty());
    }

    private Result<Long, ApplicationError> provisionAdmin(String email) {
        return facade.provisionAdminProfile(USER_ID, email, "Ana", "Torres", "987654321", "12345678");
    }

    private Result<Long, ApplicationError> provisionBusiness(String companyEmail) {
        return facade.provisionBusinessProfile(USER_ID, "Spot Gym", "20123456789", "SAC",
                "987654321", companyEmail, "Av. Larco 123", "Lima", "Miraflores");
    }

    @Test
    void returnsTheUserIdWhenTheAdminProfileIsCreated() {
        when(adminCommandService.handle(any(CreateAdminCommand.class))).thenReturn(Result.success(mock(Admin.class)));

        var result = provisionAdmin("owner@spotgym.pe");

        assertEquals(Result.success(USER_ID), result);
    }

    @Test
    void treatsAnExistingAdminProfileAsSuccessWithoutCreatingAnother() {
        when(adminQueryService.handle(any(GetAdminByUserIdQuery.class))).thenReturn(Optional.of(mock(Admin.class)));

        var result = provisionAdmin("owner@spotgym.pe");

        assertEquals(Result.success(USER_ID), result);
        verify(adminCommandService, never()).handle(any(CreateAdminCommand.class));
    }

    @Test
    void returnsTheFailureInsteadOfThrowingWhenTheAdminProfileIsRejected() {
        var error = ApplicationError.validationError("Admin", "dni already used");
        when(adminCommandService.handle(any(CreateAdminCommand.class))).thenReturn(Result.failure(error));

        var result = provisionAdmin("owner@spotgym.pe");

        assertEquals(Result.failure(error), result);
    }

    @Test
    void returnsAValidationFailureWhenTheAdminEmailIsMalformed() {
        var result = provisionAdmin("not-an-email");

        assertInstanceOf(Result.Failure.class, result);
        verify(adminCommandService, never()).handle(any(CreateAdminCommand.class));
    }

    @Test
    void treatsAnExistingBusinessProfileAsSuccess() {
        when(businessProfileCommandService.handle(any(CreateBusinessProfileCommand.class)))
                .thenReturn(Result.failure(ApplicationError.conflict("BusinessProfile", "already exists")));

        var result = provisionBusiness("info@spotgym.pe");

        assertEquals(Result.success(USER_ID), result);
    }

    @Test
    void returnsTheFailureInsteadOfThrowingWhenTheBusinessProfileIsRejected() {
        var error = ApplicationError.unexpected("BusinessProfile", "database down");
        when(businessProfileCommandService.handle(any(CreateBusinessProfileCommand.class)))
                .thenReturn(Result.failure(error));

        var result = provisionBusiness("info@spotgym.pe");

        assertEquals(Result.failure(error), result);
    }

    @Test
    void returnsAValidationFailureWhenTheCompanyEmailIsMalformed() {
        var result = provisionBusiness("not-an-email");

        assertTrue(result instanceof Result.Failure<Long, ApplicationError> f
                && "VALIDATION_ERROR".equals(f.error().code()));
        verify(businessProfileCommandService, never()).handle(any(CreateBusinessProfileCommand.class));
    }
}
