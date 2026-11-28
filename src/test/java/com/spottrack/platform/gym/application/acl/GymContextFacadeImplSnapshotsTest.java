package com.spottrack.platform.gym.application.acl;

import com.spottrack.platform.gym.application.commandServices.EquipmentCommandService;
import com.spottrack.platform.gym.application.commandServices.GymCommandService;
import com.spottrack.platform.gym.application.queryservices.EquipmentQueryService;
import com.spottrack.platform.gym.application.queryservices.GymQueryService;
import com.spottrack.platform.gym.domain.model.aggregates.Equipment;
import com.spottrack.platform.gym.domain.model.queries.GetAvailableAlternativesQuery;
import com.spottrack.platform.gym.domain.model.queries.GetEquipmentById;
import com.spottrack.platform.gym.domain.model.queries.GetEquipmentsByAdminUserIdQuery;
import com.spottrack.platform.gym.domain.model.queries.GetEquipmentsByBranchIdQuery;
import com.spottrack.platform.gym.domain.model.queries.GetEquipmentsByGymIdQuery;
import com.spottrack.platform.gym.domain.model.valueobjects.EquipmentId;
import com.spottrack.platform.gym.domain.model.valueobjects.ZoneId;
import com.spottrack.platform.gym.infrastructure.persistence.jpa.entities.BranchPersistenceEntity;
import com.spottrack.platform.gym.infrastructure.persistence.jpa.repositories.BranchPersistenceRepository;
import com.spottrack.platform.gym.infrastructure.persistence.jpa.repositories.GymWhitelistPersistenceRepository;
import com.spottrack.platform.gym.infrastructure.persistence.jpa.repositories.ZonePersistenceRepository;
import com.spottrack.platform.gym.interfaces.acl.BranchSnapshot;
import com.spottrack.platform.gym.interfaces.acl.EquipmentSnapshot;
import com.spottrack.platform.shared.domain.model.valueobjects.EquipmentStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GymContextFacadeImplSnapshotsTest {

    private EquipmentQueryService equipmentQueryService;
    private BranchPersistenceRepository branchRepository;
    private GymContextFacadeImpl facade;

    @BeforeEach
    void setUp() {
        equipmentQueryService = mock(EquipmentQueryService.class);
        branchRepository = mock(BranchPersistenceRepository.class);
        facade = new GymContextFacadeImpl(
                mock(EquipmentCommandService.class),
                equipmentQueryService,
                mock(GymCommandService.class),
                mock(GymQueryService.class),
                mock(GymWhitelistPersistenceRepository.class),
                mock(ZonePersistenceRepository.class),
                branchRepository);
    }

    @Test
    void findEquipmentSnapshotByIdCopiesTheFieldsOtherContextsRead() {
        var equipment = equipment("eq-1", "Treadmill", "T-100", EquipmentStatus.OCCUPIED, new ZoneId("zone-1"));
        when(equipmentQueryService.handle(any(GetEquipmentById.class))).thenReturn(Optional.of(equipment));

        var snapshot = facade.findEquipmentSnapshotById("eq-1");

        assertEquals(Optional.of(new EquipmentSnapshot("eq-1", "Treadmill", "T-100", EquipmentStatus.OCCUPIED, "zone-1")),
                snapshot);
    }

    @Test
    void findEquipmentSnapshotByIdKeepsAnUnplacedEquipmentWithoutZone() {
        var equipment = equipment("eq-2", "Bike", null, EquipmentStatus.AVAILABLE, null);
        when(equipmentQueryService.handle(any(GetEquipmentById.class))).thenReturn(Optional.of(equipment));

        assertEquals(null, facade.findEquipmentSnapshotById("eq-2").orElseThrow().zoneId());
    }

    @Test
    void findEquipmentSnapshotByIdIsEmptyWhenTheEquipmentDoesNotExist() {
        when(equipmentQueryService.handle(any(GetEquipmentById.class))).thenReturn(Optional.empty());

        assertTrue(facade.findEquipmentSnapshotById("ghost").isEmpty());
    }

    @Test
    void findAvailableAlternativeSnapshotsMapsEveryAlternative() {
        var alternative = equipment("eq-3", "Treadmill", "T-200", EquipmentStatus.AVAILABLE, new ZoneId("zone-2"));
        when(equipmentQueryService.handle(any(GetAvailableAlternativesQuery.class))).thenReturn(List.of(alternative));

        var alternatives = facade.findAvailableAlternativeSnapshots("Treadmill", "eq-1");

        assertEquals(List.of(new EquipmentSnapshot("eq-3", "Treadmill", "T-200", EquipmentStatus.AVAILABLE, "zone-2")),
                alternatives);
    }

    @Test
    void findBranchSnapshotsByGymIdMapsEveryBranchOfTheGym() {
        var entity = new BranchPersistenceEntity();
        entity.setBranchId("b1");
        entity.setGymId("gym-1");
        entity.setName("San Isidro");
        entity.setAddress("Av. Rivera 100");
        when(branchRepository.findByGymId("gym-1")).thenReturn(List.of(entity));

        assertEquals(List.of(new BranchSnapshot("b1", "gym-1", "San Isidro", "Av. Rivera 100")),
                facade.findBranchSnapshotsByGymId("gym-1"));
    }

    @Test
    void equipmentSnapshotsByAdminGymAndBranchUseTheMatchingQuery() {
        var admins = equipment("eq-a", "Rower", "R-1", EquipmentStatus.AVAILABLE, new ZoneId("zone-a"));
        var gyms = equipment("eq-g", "Press", "P-1", EquipmentStatus.AVAILABLE, new ZoneId("zone-g"));
        var branches = equipment("eq-b", "Bike", "B-1", EquipmentStatus.MAINTENANCE, new ZoneId("zone-b"));
        when(equipmentQueryService.handle(new GetEquipmentsByAdminUserIdQuery(7L))).thenReturn(List.of(admins));
        when(equipmentQueryService.handle(new GetEquipmentsByGymIdQuery("gym-1"))).thenReturn(List.of(gyms));
        when(equipmentQueryService.handle(new GetEquipmentsByBranchIdQuery("b1"))).thenReturn(List.of(branches));

        assertEquals("eq-a", facade.findEquipmentSnapshotsByAdminUserId(7L).getFirst().equipmentId());
        assertEquals("eq-g", facade.findEquipmentSnapshotsByGymId("gym-1").getFirst().equipmentId());
        assertEquals("eq-b", facade.findEquipmentSnapshotsByBranchId("b1").getFirst().equipmentId());
    }

    @Test
    void resolveGymIdForBranchReadsTheBranchGym() {
        var entity = new BranchPersistenceEntity();
        entity.setBranchId("b1");
        entity.setGymId("gym-1");
        when(branchRepository.findByBranchId("b1")).thenReturn(Optional.of(entity));

        assertEquals(Optional.of("gym-1"), facade.resolveGymIdForBranch("b1"));
    }

    private static Equipment equipment(String id, String name, String model, EquipmentStatus status, ZoneId zoneId) {
        var equipment = mock(Equipment.class);
        when(equipment.getId()).thenReturn(new EquipmentId(id));
        when(equipment.getEquipmentName()).thenReturn(name);
        when(equipment.getModel()).thenReturn(model);
        when(equipment.getStatus()).thenReturn(status);
        when(equipment.getZoneId()).thenReturn(zoneId);
        return equipment;
    }
}
