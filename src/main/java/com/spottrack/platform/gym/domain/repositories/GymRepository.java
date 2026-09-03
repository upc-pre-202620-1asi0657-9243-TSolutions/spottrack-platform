package com.spottrack.platform.gym.domain.repositories;
import com.spottrack.platform.gym.domain.model.aggregates.Gym;
import com.spottrack.platform.gym.domain.model.valueobjects.GymId;

import java.util.List;
import java.util.Optional;

public interface GymRepository {
    Gym save(Gym gym);
    Optional<Gym> findById(GymId gymId);
    List<Gym> findByAdminUserId(Long adminUserId);
    List<Gym> findAll();
}
