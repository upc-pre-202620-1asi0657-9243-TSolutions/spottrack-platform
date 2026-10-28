package com.spottrack.platform.gym.application.acl;

import com.spottrack.platform.gym.application.commandServices.EquipmentCommandService;
import com.spottrack.platform.gym.application.commandServices.GymCommandService;
import com.spottrack.platform.gym.application.queryservices.EquipmentQueryService;
import com.spottrack.platform.gym.application.queryservices.GymQueryService;
import com.spottrack.platform.gym.domain.model.aggregates.Gym;
import com.spottrack.platform.gym.domain.model.queries.GetGymById;
import com.spottrack.platform.gym.infrastructure.persistence.jpa.entities.BranchPersistenceEntity;
import com.spottrack.platform.gym.infrastructure.persistence.jpa.repositories.BranchPersistenceRepository;
import com.spottrack.platform.gym.infrastructure.persistence.jpa.repositories.EquipmentPersistenceRepository;
import com.spottrack.platform.gym.infrastructure.persistence.jpa.repositories.GymWhitelistPersistenceRepository;
import com.spottrack.platform.gym.infrastructure.persistence.jpa.repositories.ZonePersistenceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GymContextFacadeImplNamesTest {

    private GymQueryService gymQueryService;
    private BranchPersistenceRepository branchRepository;
    private GymContextFacadeImpl facade;

    @BeforeEach
    void setUp() {
        gymQueryService = mock(GymQueryService.class);
        branchRepository = mock(BranchPersistenceRepository.class);
        facade = new GymContextFacadeImpl(
                mock(EquipmentCommandService.class),
                mock(EquipmentQueryService.class),
                mock(GymCommandService.class),
                gymQueryService,
                mock(GymWhitelistPersistenceRepository.class),
                mock(ZonePersistenceRepository.class),
                branchRepository,
                mock(EquipmentPersistenceRepository.class));
    }

    @Test
    void findGymNameReturnsTheNameOfAnExistingGym() {
        var gym = new Gym("Gym Central", 42L);
        when(gymQueryService.handle(any(GetGymById.class))).thenReturn(Optional.of(gym));

        assertEquals(Optional.of("Gym Central"), facade.findGymName(gym.getId().uuid()));
    }

    @Test
    void findGymNameIsEmptyForAnUnknownGym() {
        when(gymQueryService.handle(any(GetGymById.class))).thenReturn(Optional.empty());

        assertTrue(facade.findGymName("ghost").isEmpty());
    }

    @Test
    void findBranchNameReturnsTheNameOfAnExistingBranch() {
        var branch = new BranchPersistenceEntity();
        branch.setName("San Isidro");
        when(branchRepository.findByBranchId("branch-1")).thenReturn(Optional.of(branch));

        assertEquals(Optional.of("San Isidro"), facade.findBranchName("branch-1"));
    }

    @Test
    void findBranchNameIsEmptyForAnUnknownBranch() {
        when(branchRepository.findByBranchId("ghost")).thenReturn(Optional.empty());

        assertTrue(facade.findBranchName("ghost").isEmpty());
    }
}
