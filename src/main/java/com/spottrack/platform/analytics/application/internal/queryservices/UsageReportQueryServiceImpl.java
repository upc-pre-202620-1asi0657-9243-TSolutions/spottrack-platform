package com.spottrack.platform.analytics.application.internal.queryservices;

import com.spottrack.platform.analytics.application.queryservices.UsageAnalyticsQueryService;
import com.spottrack.platform.analytics.application.queryservices.UsageReportQueryService;
import com.spottrack.platform.analytics.domain.model.queries.GetUsageHeatmapQuery;
import com.spottrack.platform.analytics.domain.model.queries.GetUsageReportQuery;
import com.spottrack.platform.analytics.domain.model.valueobjects.UsageReport;
import com.spottrack.platform.gym.domain.model.aggregates.Equipment;
import com.spottrack.platform.gym.interfaces.acl.GymContextFacade;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class UsageReportQueryServiceImpl implements UsageReportQueryService {

    private final UsageAnalyticsQueryService usageAnalyticsQueryService;
    private final GymContextFacade gymContextFacade;

    public UsageReportQueryServiceImpl(UsageAnalyticsQueryService usageAnalyticsQueryService,
                                       GymContextFacade gymContextFacade) {
        this.usageAnalyticsQueryService = usageAnalyticsQueryService;
        this.gymContextFacade = gymContextFacade;
    }

    @Override
    public UsageReport handle(GetUsageReportQuery query) {
        var heatmap = usageAnalyticsQueryService.handle(
                new GetUsageHeatmapQuery(query.gymId(), query.branchId(), query.from(), query.to()));

        var equipment = query.branchId() != null
                ? gymContextFacade.findEquipmentsByBranchId(query.branchId())
                : gymContextFacade.findEquipmentsByGymId(query.gymId());
        Map<String, String> equipmentNames = new HashMap<>();
        for (Equipment e : equipment) {
            equipmentNames.put(e.getId().uuid(), e.getEquipmentName());
        }

        var gymName = gymContextFacade.findGymName(query.gymId()).orElse(null);
        var branchName = query.branchId() != null
                ? gymContextFacade.findBranchName(query.branchId()).orElse(null)
                : null;
        return new UsageReport(gymName, branchName, LocalDateTime.now(), heatmap, equipmentNames);
    }
}
