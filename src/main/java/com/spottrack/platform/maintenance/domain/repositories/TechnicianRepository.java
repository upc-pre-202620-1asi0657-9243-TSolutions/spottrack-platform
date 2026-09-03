package com.spottrack.platform.maintenance.domain.repositories;

import com.spottrack.platform.maintenance.domain.model.aggregates.Technician;
import com.spottrack.platform.maintenance.domain.model.valueobjects.TechnicianId;

import java.util.List;
import java.util.Optional;

public interface TechnicianRepository {
    Optional<Technician> findById(TechnicianId id);
    boolean existsById(TechnicianId id);
    List<Technician> findAll();
    Technician save(Technician technician);
}
