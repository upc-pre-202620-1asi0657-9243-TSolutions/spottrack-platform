package com.spottrack.platform.analytics.application.internal.queryservices;

import com.spottrack.platform.analytics.application.queryservices.ROIProjectionQueryService;
import com.spottrack.platform.analytics.domain.model.aggregates.ROIProjection;
import com.spottrack.platform.analytics.domain.model.queries.GetAllROIProjectionsQuery;
import com.spottrack.platform.analytics.domain.repositories.ROIProjectionRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ROIProjectionQueryServiceImpl implements ROIProjectionQueryService {

    private final ROIProjectionRepository roiProjectionRepository;

    public ROIProjectionQueryServiceImpl(ROIProjectionRepository roiProjectionRepository) {
        this.roiProjectionRepository = roiProjectionRepository;
    }

    @Override
    public List<ROIProjection> handle(GetAllROIProjectionsQuery query) {
        return roiProjectionRepository.findAll();
    }
}
