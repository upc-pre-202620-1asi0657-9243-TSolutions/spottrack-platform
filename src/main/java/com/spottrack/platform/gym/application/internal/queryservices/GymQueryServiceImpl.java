package com.spottrack.platform.gym.application.internal.queryservices;

import com.spottrack.platform.gym.application.queryservices.GymQueryService;
import com.spottrack.platform.gym.domain.model.aggregates.Gym;
import com.spottrack.platform.gym.domain.model.entities.Branch;
import com.spottrack.platform.gym.domain.model.entities.GymWhitelistEntry;
import com.spottrack.platform.gym.domain.model.entities.Zone;
import com.spottrack.platform.gym.domain.model.queries.GetAllGymsQuery;
import com.spottrack.platform.gym.domain.model.queries.GetBranchesByGymIdQuery;
import com.spottrack.platform.gym.domain.model.queries.GetGymById;
import com.spottrack.platform.gym.domain.model.queries.GetGymsByAdminUserId;
import com.spottrack.platform.gym.domain.model.queries.GetWhitelistByGymIdQuery;
import com.spottrack.platform.gym.domain.model.queries.GetZonesByGymIdQuery;
import com.spottrack.platform.gym.domain.model.valueobjects.Dni;
import com.spottrack.platform.gym.infrastructure.persistence.jpa.assemblers.BranchPersistenceAssembler;
import com.spottrack.platform.gym.infrastructure.persistence.jpa.assemblers.GymPersistenceAssembler;
import com.spottrack.platform.gym.infrastructure.persistence.jpa.assemblers.ZonePersistenceAssembler;
import com.spottrack.platform.gym.infrastructure.persistence.jpa.repositories.BranchPersistenceRepository;
import com.spottrack.platform.gym.infrastructure.persistence.jpa.repositories.GymPersistenceRepository;
import com.spottrack.platform.gym.infrastructure.persistence.jpa.repositories.GymWhitelistPersistenceRepository;
import com.spottrack.platform.gym.infrastructure.persistence.jpa.repositories.ZonePersistenceRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class GymQueryServiceImpl implements GymQueryService {
    GymPersistenceRepository gymPersistenceRepository;
    GymWhitelistPersistenceRepository gymWhitelistPersistenceRepository;
    BranchPersistenceRepository branchPersistenceRepository;
    ZonePersistenceRepository zonePersistenceRepository;

    public GymQueryServiceImpl(GymPersistenceRepository gymPersistenceRepository,
                               GymWhitelistPersistenceRepository gymWhitelistPersistenceRepository,
                               BranchPersistenceRepository branchPersistenceRepository,
                               ZonePersistenceRepository zonePersistenceRepository) {
        this.gymPersistenceRepository = gymPersistenceRepository;
        this.gymWhitelistPersistenceRepository = gymWhitelistPersistenceRepository;
        this.branchPersistenceRepository = branchPersistenceRepository;
        this.zonePersistenceRepository = zonePersistenceRepository;
    }

    @Override
    public Optional<Gym> handle(GetGymById query) {
        return gymPersistenceRepository.findByGymId(query.id().uuid())
                .map(GymPersistenceAssembler::toDomainFromPersistence);
    }

    @Override
    public List<Gym> handle(GetGymsByAdminUserId query) {
        return gymPersistenceRepository.findByAdminUserId(query.adminUserId()).stream()
                .map(GymPersistenceAssembler::toDomainFromPersistence)
                .toList();
    }

    @Override
    public List<Gym> handle(GetAllGymsQuery query) {
        // TODO: add pagination if gym volume grows
        return gymPersistenceRepository.findAll().stream()
                .map(GymPersistenceAssembler::toDomainFromPersistence)
                .toList();
    }

    @Override
    public List<GymWhitelistEntry> handle(GetWhitelistByGymIdQuery query) {
        return gymWhitelistPersistenceRepository.findByGymId(query.gymId()).stream()
                .map(e -> new GymWhitelistEntry(e.getId(), e.getGymId(), new Dni(e.getDni())))
                .toList();
    }

    @Override
    public List<Branch> handle(GetBranchesByGymIdQuery query) {
        return branchPersistenceRepository.findByGymId(query.gymId()).stream()
                .map(BranchPersistenceAssembler::toDomainFromPersistence)
                .toList();
    }

    @Override
    public List<Zone> handle(GetZonesByGymIdQuery query) {
        var branchIds = branchPersistenceRepository.findByGymId(query.gymId()).stream()
                .map(b -> b.getBranchId())
                .toList();
        return zonePersistenceRepository.findByBranchIdIn(branchIds).stream()
                .map(ZonePersistenceAssembler::toDomainFromPersistence)
                .toList();
    }
}
