package com.spottrack.platform.maintenance.application.internal.queryservices;

import com.spottrack.platform.maintenance.application.queryservices.TechnicianQueryService;
import com.spottrack.platform.maintenance.domain.model.aggregates.Technician;
import com.spottrack.platform.maintenance.domain.model.queries.GetAllTechniciansQuery;
import com.spottrack.platform.maintenance.domain.model.queries.GetTechnicianByTechnicianIdQuery;
import com.spottrack.platform.maintenance.domain.model.queries.GetTechnicianByUserIdQuery;
import com.spottrack.platform.maintenance.domain.model.valueobjects.TechnicianId;
import com.spottrack.platform.maintenance.domain.model.queries.GetTechniciansByAdminQuery;
import com.spottrack.platform.maintenance.domain.repositories.TechnicianRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TechnicianQueryServiceImpl implements TechnicianQueryService {

    private final TechnicianRepository technicianRepository;

    public TechnicianQueryServiceImpl(TechnicianRepository technicianRepository) {
        this.technicianRepository = technicianRepository;
    }

    @Override
    public List<Technician> handle(GetAllTechniciansQuery query) {
        return technicianRepository.findAll();
    }

    @Override
    public List<Technician> handle(GetTechniciansByAdminQuery query) {
        return technicianRepository.findByAdminUserIdOrLegacy(query.adminUserId());
    }

    @Override
    public Optional<Technician> handle(GetTechnicianByUserIdQuery query) {
        return technicianRepository.findByUserId(query.userId());
    }

    @Override
    public Optional<Technician> handle(GetTechnicianByTechnicianIdQuery query) {
        return technicianRepository.findById(new TechnicianId(query.technicianId()));
    }
}
