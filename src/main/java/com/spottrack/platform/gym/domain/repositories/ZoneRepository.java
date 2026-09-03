package com.spottrack.platform.gym.domain.repositories;

import com.spottrack.platform.gym.domain.model.entities.Zone;

import java.util.Optional;

public interface ZoneRepository {
    Zone save(Zone zone);
    Optional<Zone> findById(String zoneId);
    Optional<Zone> findByName(String zoneName);
}
