package com.spottrack.platform.iam.application.acl;

import com.spottrack.platform.iam.application.commandservices.PendingRegistrationCommandService;
import com.spottrack.platform.iam.application.commandservices.UserCommandService;
import com.spottrack.platform.iam.application.queryservices.UserQueryService;
import com.spottrack.platform.iam.domain.model.commands.SavePendingRegistrationCommand;
import com.spottrack.platform.iam.domain.repositories.PendingRegistrationRepository;
import com.spottrack.platform.iam.interfaces.acl.PendingRegistrationRequest;
import com.spottrack.platform.shared.application.result.ApplicationError;
import com.spottrack.platform.shared.application.result.Result;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class IamContextFacadeImplPendingRegistrationTest {

    private PendingRegistrationCommandService pendingRegistrationCommandService;
    private IamContextFacadeImpl facade;

    @BeforeEach
    void setUp() {
        pendingRegistrationCommandService = mock(PendingRegistrationCommandService.class);
        facade = new IamContextFacadeImpl(
                mock(UserQueryService.class),
                mock(UserCommandService.class),
                pendingRegistrationCommandService,
                mock(PendingRegistrationRepository.class));
    }

    private static PendingRegistrationRequest request(String companyName) {
        return new PendingRegistrationRequest(
                "owner@spotgym.pe", "s3cret!", "Ana", "Torres", "987654321", "12345678",
                companyName, "20123456789", "SAC", "014445566", "info@spotgym.pe",
                "Av. Larco 123", "Lima", "Miraflores", "BASIC");
    }

    @Test
    void buildsTheIamCommandFromTheRequestAndReturnsThePendingId() {
        var pendingId = UUID.randomUUID();
        when(pendingRegistrationCommandService.handle(any(SavePendingRegistrationCommand.class)))
                .thenReturn(Result.success(pendingId));

        var result = facade.savePendingRegistration(request("Spot Gym"));

        assertEquals(Result.success(pendingId), result);
        var captor = ArgumentCaptor.forClass(SavePendingRegistrationCommand.class);
        verify(pendingRegistrationCommandService).handle(captor.capture());
        assertEquals("owner@spotgym.pe", captor.getValue().email());
        assertEquals("Spot Gym", captor.getValue().companyName());
        assertEquals("BASIC", captor.getValue().membershipTier());
    }

    @Test
    void returnsAValidationFailureInsteadOfThrowingWhenARequiredFieldIsBlank() {
        var result = facade.savePendingRegistration(request(" "));

        assertTrue(result instanceof Result.Failure<UUID, ApplicationError> f
                && "VALIDATION_ERROR".equals(f.error().code()));
        verify(pendingRegistrationCommandService, never()).handle(any(SavePendingRegistrationCommand.class));
    }
}
