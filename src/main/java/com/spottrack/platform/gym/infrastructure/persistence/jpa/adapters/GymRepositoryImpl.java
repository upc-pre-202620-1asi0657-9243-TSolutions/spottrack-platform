package com.spottrack.platform.gym.infrastructure.persistence.jpa.adapters;

import com.spottrack.platform.gym.domain.model.aggregates.Gym;
import com.spottrack.platform.gym.domain.model.valueobjects.GymId;
import com.spottrack.platform.gym.domain.repositories.GymRepository;
import com.spottrack.platform.gym.infrastructure.persistence.jpa.assemblers.GymPersistenceAssembler;
import com.spottrack.platform.gym.infrastructure.persistence.jpa.repositories.GymPersistenceRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class GymRepositoryImpl implements GymRepository {
    private final GymPersistenceRepository gymPersistenceRepository;

    public GymRepositoryImpl(GymPersistenceRepository gymPersistenceRepository) {
        this.gymPersistenceRepository = gymPersistenceRepository;
    }

    @Override
    public Gym save(Gym gym) {
        var entity = GymPersistenceAssembler.toPersistenceFromDomain(gym);
        var saved = gymPersistenceRepository.save(entity);
        return GymPersistenceAssembler.toDomainFromPersistence(saved);
    }

    @Override
    public Optional<Gym> findById(GymId gymId) {
        return gymPersistenceRepository.findByGymId(gymId.uuid())
                .map(GymPersistenceAssembler::toDomainFromPersistence);
    }

    @Override
    public List<Gym> findByAdminUserId(Long adminUserId) {
        return gymPersistenceRepository.findByAdminUserId(adminUserId).stream()
                .map(GymPersistenceAssembler::toDomainFromPersistence)
                .toList();
    }

    @Override
    public List<Gym> findAll() {
        return gymPersistenceRepository.findAll().stream()
                .map(GymPersistenceAssembler::toDomainFromPersistence)
                .toList();
    }
}
