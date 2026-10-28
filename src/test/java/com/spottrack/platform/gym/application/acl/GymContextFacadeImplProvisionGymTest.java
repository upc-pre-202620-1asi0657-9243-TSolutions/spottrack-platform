package com.spottrack.platform.gym.application.acl;

import com.spottrack.platform.gym.application.commandServices.EquipmentCommandService;
import com.spottrack.platform.gym.application.commandServices.GymCommandService;
import com.spottrack.platform.gym.application.queryservices.EquipmentQueryService;
import com.spottrack.platform.gym.application.queryservices.GymQueryService;
import com.spottrack.platform.gym.domain.model.aggregates.Gym;
import com.spottrack.platform.gym.domain.model.commands.CreateGym;
import com.spottrack.platform.gym.domain.model.queries.GetGymsByAdminUserId;
import com.spottrack.platform.gym.infrastructure.persistence.jpa.repositories.BranchPersistenceRepository;
import com.spottrack.platform.gym.infrastructure.persistence.jpa.repositories.EquipmentPersistenceRepository;
import com.spottrack.platform.gym.infrastructure.persistence.jpa.repositories.GymWhitelistPersistenceRepository;
import com.spottrack.platform.gym.infrastructure.persistence.jpa.repositories.ZonePersistenceRepository;
import com.spottrack.platform.shared.application.result.ApplicationError;
import com.spottrack.platform.shared.application.result.Result;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class GymContextFacadeImplProvisionGymTest {

    private static final Long ADMIN_ID = 42L;

    private GymCommandService gymCommandService;
    private GymQueryService gymQueryService;
    private GymContextFacadeImpl facade;

    @BeforeEach
    void setUp() {
        gymCommandService = mock(GymCommandService.class);
        gymQueryService = mock(GymQueryService.class);
        facade = new GymContextFacadeImpl(
                mock(EquipmentCommandService.class),
                mock(EquipmentQueryService.class),
                gymCommandService,
                gymQueryService,
                mock(GymWhitelistPersistenceRepository.class),
                mock(ZonePersistenceRepository.class),
                mock(BranchPersistenceRepository.class),
                mock(EquipmentPersistenceRepository.class));
    }

    @Test
    void createsTheGymForTheAdminWhenNoneWithThatNameExists() {
        when(gymQueryService.handle(any(GetGymsByAdminUserId.class))).thenReturn(List.of());
        when(gymCommandService.handle(any(CreateGym.class)))
                .thenReturn(Result.success(new Gym("Spot Gym Miraflores", ADMIN_ID)));

        facade.provisionGym(ADMIN_ID, "Spot Gym Miraflores");

        var captor = ArgumentCaptor.forClass(CreateGym.class);
        verify(gymCommandService).handle(captor.capture());
        assertEquals("Spot Gym Miraflores", captor.getValue().gymName());
        assertEquals(ADMIN_ID, captor.getValue().adminUserId());
    }

    @Test
    void doesNotCreateASecondGymWithTheSameNameForTheSameAdmin() {
        when(gymQueryService.handle(any(GetGymsByAdminUserId.class)))
                .thenReturn(List.of(new Gym("spot gym miraflores", ADMIN_ID)));

        facade.provisionGym(ADMIN_ID, "Spot Gym Miraflores");

        verify(gymCommandService, never()).handle(any(CreateGym.class));
    }

    @Test
    void failsLoudlyWhenTheGymCannotBeCreated() {
        when(gymQueryService.handle(any(GetGymsByAdminUserId.class))).thenReturn(List.of());
        when(gymCommandService.handle(any(CreateGym.class)))
                .thenReturn(Result.failure(ApplicationError.unexpected("Gym", "boom")));

        assertThrows(RuntimeException.class, () -> facade.provisionGym(ADMIN_ID, "Spot Gym Miraflores"));
    }
}
