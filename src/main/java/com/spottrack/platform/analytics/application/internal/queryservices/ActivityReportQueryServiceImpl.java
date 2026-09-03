package com.spottrack.platform.analytics.application.internal.queryservices;

import com.spottrack.platform.analytics.application.queryservices.ActivityReportQueryService;
import com.spottrack.platform.analytics.domain.model.aggregates.ActivityReport;
import com.spottrack.platform.analytics.domain.model.queries.GetAllActivityReportsQuery;
import com.spottrack.platform.analytics.domain.repositories.ActivityReportRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ActivityReportQueryServiceImpl implements ActivityReportQueryService {

    private final ActivityReportRepository activityReportRepository;

    public ActivityReportQueryServiceImpl(ActivityReportRepository activityReportRepository) {
        this.activityReportRepository = activityReportRepository;
    }

    @Override
    public List<ActivityReport> handle(GetAllActivityReportsQuery query) {
        return activityReportRepository.findAll();
    }
}
