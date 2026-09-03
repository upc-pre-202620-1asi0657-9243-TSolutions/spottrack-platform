package com.spottrack.platform.gym.application.internal.commandservices;

import com.spottrack.platform.gym.application.commandServices.GymCommandService;
import com.spottrack.platform.gym.domain.model.aggregates.Equipment;
import com.spottrack.platform.gym.domain.model.aggregates.Gym;
import com.spottrack.platform.gym.domain.model.commands.AddBranchCommand;
import com.spottrack.platform.gym.domain.model.commands.AddDniToWhitelistCommand;
import com.spottrack.platform.gym.domain.model.commands.AddZoneCommand;
import com.spottrack.platform.gym.domain.model.commands.CreateGym;
import com.spottrack.platform.gym.domain.model.commands.RemoveDniFromWhitelistCommand;
import com.spottrack.platform.gym.domain.model.commands.RequestEquipmentRelocation;
import com.spottrack.platform.gym.domain.model.entities.Branch;
import com.spottrack.platform.gym.domain.model.entities.GymWhitelistEntry;
import com.spottrack.platform.gym.domain.model.entities.Zone;
import com.spottrack.platform.gym.infrastructure.persistence.jpa.assemblers.BranchPersistenceAssembler;
import com.spottrack.platform.gym.infrastructure.persistence.jpa.assemblers.GymPersistenceAssembler;
import com.spottrack.platform.gym.infrastructure.persistence.jpa.assemblers.ZonePersistenceAssembler;
import com.spottrack.platform.gym.infrastructure.persistence.jpa.entities.GymWhitelistPersistenceEntity;
import com.spottrack.platform.gym.infrastructure.persistence.jpa.entities.ZonePersistenceEntity;
import com.spottrack.platform.gym.infrastructure.persistence.jpa.repositories.BranchPersistenceRepository;
import com.spottrack.platform.gym.infrastructure.persistence.jpa.repositories.EquipmentPersistenceRepository;
import com.spottrack.platform.gym.infrastructure.persistence.jpa.repositories.GymPersistenceRepository;
import com.spottrack.platform.gym.infrastructure.persistence.jpa.repositories.GymWhitelistPersistenceRepository;
import com.spottrack.platform.gym.infrastructure.persistence.jpa.repositories.ZonePersistenceRepository;
import com.spottrack.platform.membership.interfaces.acl.MembershipContextFacade;
import com.spottrack.platform.shared.application.result.ApplicationError;
import com.spottrack.platform.shared.application.result.Result;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class GymCommandServiceImpl implements GymCommandService {
    GymPersistenceRepository gymPersistenceRepository;
    EquipmentPersistenceRepository equipmentPersistenceRepository;
    BranchPersistenceRepository branchPersistenceRepository;
    ZonePersistenceRepository zonePersistenceRepository;
    GymWhitelistPersistenceRepository gymWhitelistPersistenceRepository;
    MembershipContextFacade membershipContextFacade;

    public GymCommandServiceImpl(GymPersistenceRepository gymPersistenceRepository,
                                 EquipmentPersistenceRepository equipmentPersistenceRepository,
                                 BranchPersistenceRepository branchPersistenceRepository,
                                 ZonePersistenceRepository zonePersistenceRepository,
                                 GymWhitelistPersistenceRepository gymWhitelistPersistenceRepository,
                                 MembershipContextFacade membershipContextFacade) {
        this.gymPersistenceRepository = gymPersistenceRepository;
        this.zonePersistenceRepository = zonePersistenceRepository;
        this.equipmentPersistenceRepository = equipmentPersistenceRepository;
        this.branchPersistenceRepository = branchPersistenceRepository;
        this.gymWhitelistPersistenceRepository = gymWhitelistPersistenceRepository;
        this.membershipContextFacade = membershipContextFacade;
    }

    @Override
    public Result<Equipment, ApplicationError> handle(RequestEquipmentRelocation command) {
        return null;
    }

    @Transactional
    @Override
    public Result<Gym, ApplicationError> handle(CreateGym command) {
        var gym = new Gym(command.gymName(), command.adminUserId());
        var gymEntity = GymPersistenceAssembler.toPersistenceFromDomain(gym);
        var savedEntity = gymPersistenceRepository.save(gymEntity);
        return Result.success(GymPersistenceAssembler.toDomainFromPersistence(savedEntity));
    }

    @Transactional
    @Override
    public Result<Branch, ApplicationError> handle(AddBranchCommand command) {
        var gym = gymPersistenceRepository.findByGymId(command.gymId());
        if (gym.isEmpty()) {
            return Result.failure(ApplicationError.notFound("Gym", command.gymId()));
        }
        var tierOpt = membershipContextFacade.fetchActiveMembershipTier(gym.get().getAdminUserId());
        if (tierOpt.isEmpty()) {
            return Result.failure(ApplicationError.businessRuleViolation(
                    "Active membership required", "gym.error.branch.noActiveMembership"));
        }
        int currentCount = branchPersistenceRepository.countByGymId(command.gymId());
        if (currentCount >= tierOpt.get().maxBranches()) {
            return Result.failure(ApplicationError.businessRuleViolation(
                    "Branch limit", "gym.error.branch.limitReached"));
        }
        var branch = new Branch(command.gymId(), command.name(), command.address());
        var branchEntity = BranchPersistenceAssembler.toPersistenceFromDomain(branch);
        branchPersistenceRepository.save(branchEntity);
        return Result.success(branch);
    }

    @Transactional
    @Override
    public Result<Zone, ApplicationError> handle(AddZoneCommand command) {
        var zone = new Zone(command.zoneName(), command.maximumOccupancy(), command.branchId());
        var zoneEntity = ZonePersistenceAssembler.toPersistenceFromDomain(zone);
        zonePersistenceRepository.save(zoneEntity);
        var savedZone = ZonePersistenceAssembler.toDomainFromPersistence(zoneEntity);
        return Result.success(savedZone);
    }

    @Transactional
    @Override
    public Result<GymWhitelistEntry, ApplicationError> handle(AddDniToWhitelistCommand command) {
        var dniValue = command.dni().value();
        if (gymWhitelistPersistenceRepository.existsByGymIdAndDni(command.gymId(), dniValue)) {
            return Result.failure(ApplicationError.conflict("GymWhitelist", "gym.error.whitelist.dniAlreadyExists"));
        }
        var entity = new GymWhitelistPersistenceEntity(command.gymId(), dniValue);
        var saved = gymWhitelistPersistenceRepository.save(entity);
        return Result.success(new GymWhitelistEntry(saved.getId(), saved.getGymId(), command.dni()));
    }

    @Transactional
    @Override
    public void handle(RemoveDniFromWhitelistCommand command) {
        gymWhitelistPersistenceRepository.deleteByGymIdAndDni(command.gymId(), command.dni());
    }

}
