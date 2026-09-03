package com.spottrack.platform.gym.application.acl;

import com.spottrack.platform.gym.application.commandServices.EquipmentCommandService;
import com.spottrack.platform.gym.application.queryservices.EquipmentQueryService;
import com.spottrack.platform.gym.application.queryservices.GymQueryService;
import com.spottrack.platform.gym.domain.model.aggregates.Equipment;
import com.spottrack.platform.gym.domain.model.commands.UpdateEquipmentStatus;
import com.spottrack.platform.gym.domain.model.queries.GetAvailableAlternativesQuery;
import com.spottrack.platform.gym.domain.model.queries.GetEquipmentById;
import com.spottrack.platform.gym.domain.model.queries.GetGymById;
import com.spottrack.platform.gym.domain.model.queries.GetGymsByAdminUserId;
import com.spottrack.platform.gym.domain.model.valueobjects.EquipmentId;
import com.spottrack.platform.gym.domain.model.valueobjects.EquipmentStatus;
import com.spottrack.platform.gym.domain.model.valueobjects.GymId;
import com.spottrack.platform.gym.infrastructure.persistence.jpa.assemblers.EquipmentPersistenceAssembler;
import com.spottrack.platform.gym.infrastructure.persistence.jpa.repositories.BranchPersistenceRepository;
import com.spottrack.platform.gym.infrastructure.persistence.jpa.repositories.EquipmentPersistenceRepository;
import com.spottrack.platform.gym.infrastructure.persistence.jpa.repositories.GymWhitelistPersistenceRepository;
import com.spottrack.platform.gym.infrastructure.persistence.jpa.repositories.ZonePersistenceRepository;
import com.spottrack.platform.gym.interfaces.acl.GymContextFacade;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class GymContextFacadeImpl implements GymContextFacade {

    private final EquipmentCommandService equipmentCommandService;
    private final EquipmentQueryService equipmentQueryService;
    private final GymQueryService gymQueryService;
    private final GymWhitelistPersistenceRepository gymWhitelistPersistenceRepository;
    private final ZonePersistenceRepository zonePersistenceRepository;
    private final BranchPersistenceRepository branchPersistenceRepository;
    private final EquipmentPersistenceRepository equipmentPersistenceRepository;

    public GymContextFacadeImpl(EquipmentCommandService equipmentCommandService,
                                EquipmentQueryService equipmentQueryService,
                                GymQueryService gymQueryService,
                                GymWhitelistPersistenceRepository gymWhitelistPersistenceRepository,
                                ZonePersistenceRepository zonePersistenceRepository,
                                BranchPersistenceRepository branchPersistenceRepository,
                                EquipmentPersistenceRepository equipmentPersistenceRepository) {
        this.equipmentCommandService = equipmentCommandService;
        this.equipmentQueryService = equipmentQueryService;
        this.gymQueryService = gymQueryService;
        this.gymWhitelistPersistenceRepository = gymWhitelistPersistenceRepository;
        this.zonePersistenceRepository = zonePersistenceRepository;
        this.branchPersistenceRepository = branchPersistenceRepository;
        this.equipmentPersistenceRepository = equipmentPersistenceRepository;
    }

    @Override
    public void updateEquipmentStatus(String equipmentId, EquipmentStatus status) {
        equipmentCommandService.handle(new UpdateEquipmentStatus(equipmentId, status));
    }

    @Override
    public Optional<Equipment> findEquipmentById(String equipmentId) {
        return equipmentQueryService.handle(new GetEquipmentById(new EquipmentId(equipmentId)));
    }

    @Override
    public List<Equipment> findAvailableAlternatives(String equipmentName, String excludeEquipmentId) {
        return equipmentQueryService.handle(new GetAvailableAlternativesQuery(equipmentName, excludeEquipmentId));
    }

    @Override
    public Long fetchAdminUserIdByGymId(String gymId) {
        return gymQueryService.handle(new GetGymById(new GymId(gymId)))
                .map(gym -> gym.getAdminUserId() != null ? gym.getAdminUserId() : 0L)
                .orElse(0L);
    }

    @Override
    public boolean isDniWhitelistedForGym(String gymId, String dni) {
        return gymWhitelistPersistenceRepository.existsByGymIdAndDni(gymId, dni);
    }

    @Override
    public Optional<String> resolveGymIdForZone(String zoneId) {
        return zonePersistenceRepository.findByZoneId(zoneId)
                .flatMap(zone -> branchPersistenceRepository.findByBranchId(zone.getBranchId()))
                .map(branch -> branch.getGymId());
    }

    @Override
    public Optional<String> resolveGymIdForEquipment(String equipmentId) {
        return equipmentQueryService.handle(new GetEquipmentById(new EquipmentId(equipmentId)))
                .filter(eq -> eq.getZoneId() != null)
                .flatMap(eq -> resolveGymIdForZone(eq.getZoneId().uuid()));
    }

    @Override
    public boolean isGymOwnedByAdmin(String gymId, Long adminUserId) {
        return gymQueryService.handle(new GetGymById(new GymId(gymId)))
                .map(gym -> adminUserId.equals(gym.getAdminUserId()))
                .orElse(false);
    }

    @Override
    public List<Equipment> findEquipmentsByAdminUserId(Long adminUserId) {
        return gymQueryService.handle(new GetGymsByAdminUserId(adminUserId)).stream()
                .flatMap(gym -> branchPersistenceRepository.findByGymId(gym.getId().uuid()).stream())
                .flatMap(branch -> zonePersistenceRepository.findByBranchId(branch.getBranchId()).stream())
                .flatMap(zone -> equipmentPersistenceRepository.findByZoneId(zone.getZoneId()).stream())
                .map(EquipmentPersistenceAssembler::toDomainFromPersistence)
                .toList();
    }

    @Override
    public List<Equipment> findEquipmentsByGymId(String gymId) {
        return branchPersistenceRepository.findByGymId(gymId).stream()
                .flatMap(branch -> zonePersistenceRepository.findByBranchId(branch.getBranchId()).stream())
                .flatMap(zone -> equipmentPersistenceRepository.findByZoneId(zone.getZoneId()).stream())
                .map(EquipmentPersistenceAssembler::toDomainFromPersistence)
                .toList();
    }
}
