package com.spottrack.platform.gym.application.internal.queryservices;

import com.spottrack.platform.gym.application.queryservices.GymQueryService;
import com.spottrack.platform.gym.domain.model.aggregates.Gym;
import com.spottrack.platform.gym.domain.model.queries.GetEquipmentsByAdminUserIdQuery;
import com.spottrack.platform.gym.domain.model.queries.GetEquipmentsByBranchIdQuery;
import com.spottrack.platform.gym.domain.model.queries.GetEquipmentsByGymIdQuery;
import com.spottrack.platform.gym.domain.model.queries.GetGymsByAdminUserId;
import com.spottrack.platform.gym.domain.model.valueobjects.GymId;
import com.spottrack.platform.gym.infrastructure.persistence.jpa.entities.BranchPersistenceEntity;
import com.spottrack.platform.gym.infrastructure.persistence.jpa.entities.EquipmentPersistenceEntity;
import com.spottrack.platform.gym.infrastructure.persistence.jpa.entities.ZonePersistenceEntity;
import com.spottrack.platform.gym.infrastructure.persistence.jpa.repositories.BranchPersistenceRepository;
import com.spottrack.platform.gym.infrastructure.persistence.jpa.repositories.EquipmentPersistenceRepository;
import com.spottrack.platform.gym.infrastructure.persistence.jpa.repositories.ZonePersistenceRepository;
import com.spottrack.platform.gym.domain.model.aggregates.Equipment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Equipment lookups by admin, gym and branch, used by the gym controllers and, as snapshots,
 * by the GymContextFacade.
 */
class EquipmentQueryServiceImplScopeTest {

    private EquipmentPersistenceRepository equipmentRepository;
    private ZonePersistenceRepository zoneRepository;
    private BranchPersistenceRepository branchRepository;
    private GymQueryService gymQueryService;
    private EquipmentQueryServiceImpl service;

    @BeforeEach
    void setUp() {
        equipmentRepository = mock(EquipmentPersistenceRepository.class);
        zoneRepository = mock(ZonePersistenceRepository.class);
        branchRepository = mock(BranchPersistenceRepository.class);
        gymQueryService = mock(GymQueryService.class);
        service = new EquipmentQueryServiceImpl(equipmentRepository, zoneRepository, branchRepository, gymQueryService);

        when(branchRepository.findByGymId("gym-1")).thenReturn(List.of(branch("b1"), branch("b2")));
        when(zoneRepository.findByBranchId("b1")).thenReturn(List.of(zone("z1")));
        when(zoneRepository.findByBranchId("b2")).thenReturn(List.of(zone("z2"), zone("z3")));
        when(equipmentRepository.findByZoneId("z1")).thenReturn(List.of(equipment("eq-1")));
        when(equipmentRepository.findByZoneId("z2")).thenReturn(List.of(equipment("eq-2")));
        when(equipmentRepository.findByZoneId("z3")).thenReturn(List.of(equipment("eq-3")));
    }

    @Test
    void branchScopeReturnsTheEquipmentOfEveryZoneOfTheBranch() {
        assertEquals(List.of("eq-2", "eq-3"), ids(service.handle(new GetEquipmentsByBranchIdQuery("b2"))));
    }

    @Test
    void gymScopeReturnsTheEquipmentOfEveryBranchOfTheGym() {
        assertEquals(List.of("eq-1", "eq-2", "eq-3"), ids(service.handle(new GetEquipmentsByGymIdQuery("gym-1"))));
    }

    @Test
    void adminScopeReturnsTheEquipmentOfEveryGymTheAdminOwns() {
        when(gymQueryService.handle(new GetGymsByAdminUserId(7L)))
                .thenReturn(List.of(new Gym(new GymId("gym-1"), "Gym Central", 7L)));

        assertEquals(List.of("eq-1", "eq-2", "eq-3"), ids(service.handle(new GetEquipmentsByAdminUserIdQuery(7L))));
    }

    @Test
    void adminScopeIsEmptyWhenTheAdminOwnsNoGym() {
        when(gymQueryService.handle(new GetGymsByAdminUserId(8L))).thenReturn(List.of());

        assertTrue(service.handle(new GetEquipmentsByAdminUserIdQuery(8L)).isEmpty());
    }

    private static List<String> ids(List<Equipment> equipment) {
        return equipment.stream().map(e -> e.getId().uuid()).toList();
    }

    private static BranchPersistenceEntity branch(String id) {
        var entity = new BranchPersistenceEntity();
        entity.setBranchId(id);
        entity.setGymId("gym-1");
        return entity;
    }

    private static ZonePersistenceEntity zone(String id) {
        var entity = new ZonePersistenceEntity();
        entity.setZoneId(id);
        return entity;
    }

    private static EquipmentPersistenceEntity equipment(String id) {
        var entity = new EquipmentPersistenceEntity();
        entity.setEquipmentId(id);
        entity.setEquipmentName("Machine " + id);
        return entity;
    }
}
