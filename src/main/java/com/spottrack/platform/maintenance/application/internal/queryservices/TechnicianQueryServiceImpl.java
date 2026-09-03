package com.spottrack.platform.maintenance.application.internal.queryservices;

import com.spottrack.platform.maintenance.application.queryservices.TechnicianQueryService;
import com.spottrack.platform.maintenance.domain.model.aggregates.Technician;
import com.spottrack.platform.maintenance.domain.model.queries.GetAllTechniciansQuery;
import com.spottrack.platform.maintenance.domain.repositories.TechnicianRepository;
import org.springframework.stereotype.Service;

import java.util.List;

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
}
