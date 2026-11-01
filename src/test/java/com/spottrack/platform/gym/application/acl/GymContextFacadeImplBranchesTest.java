package com.spottrack.platform.gym.application.acl;

import com.spottrack.platform.gym.application.commandServices.EquipmentCommandService;
import com.spottrack.platform.gym.application.commandServices.GymCommandService;
import com.spottrack.platform.gym.application.queryservices.EquipmentQueryService;
import com.spottrack.platform.gym.application.queryservices.GymQueryService;
import com.spottrack.platform.gym.infrastructure.persistence.jpa.entities.BranchPersistenceEntity;
import com.spottrack.platform.gym.infrastructure.persistence.jpa.repositories.BranchPersistenceRepository;
import com.spottrack.platform.gym.infrastructure.persistence.jpa.repositories.EquipmentPersistenceRepository;
import com.spottrack.platform.gym.infrastructure.persistence.jpa.repositories.GymWhitelistPersistenceRepository;
import com.spottrack.platform.gym.infrastructure.persistence.jpa.repositories.ZonePersistenceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GymContextFacadeImplBranchesTest {

    private BranchPersistenceRepository branchRepository;
    private GymContextFacadeImpl facade;

    @BeforeEach
    void setUp() {
        branchRepository = mock(BranchPersistenceRepository.class);
        facade = new GymContextFacadeImpl(
                mock(EquipmentCommandService.class),
                mock(EquipmentQueryService.class),
                mock(GymCommandService.class),
                mock(GymQueryService.class),
                mock(GymWhitelistPersistenceRepository.class),
                mock(ZonePersistenceRepository.class),
                branchRepository,
                mock(EquipmentPersistenceRepository.class));
    }

    @Test
    void findBranchesByGymIdMapsEveryBranchOfTheGym() {
        when(branchRepository.findByGymId("gym-1")).thenReturn(List.of(
                branch("b1", "gym-1", "San Isidro", "Av. Rivera 100"),
                branch("b2", "gym-1", "La Molina", "Av. Javier Prado 200")));

        var branches = facade.findBranchesByGymId("gym-1");

        assertEquals(2, branches.size());
        assertEquals("b1", branches.get(0).getId().uuid());
        assertEquals("gym-1", branches.get(0).getGymId());
        assertEquals("San Isidro", branches.get(0).getName());
        assertEquals("La Molina", branches.get(1).getName());
    }

    @Test
    void findBranchesByGymIdIsEmptyWhenTheGymHasNoneOrDoesNotExist() {
        when(branchRepository.findByGymId("ghost")).thenReturn(List.of());

        assertTrue(facade.findBranchesByGymId("ghost").isEmpty());
    }

    private static BranchPersistenceEntity branch(String branchId, String gymId, String name, String address) {
        var entity = new BranchPersistenceEntity();
        entity.setBranchId(branchId);
        entity.setGymId(gymId);
        entity.setName(name);
        entity.setAddress(address);
        return entity;
    }
}
