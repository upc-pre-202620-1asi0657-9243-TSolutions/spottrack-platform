package com.spottrack.platform.analytics.interfaces.rest.transform;

import com.spottrack.platform.analytics.domain.model.valueobjects.RelocationInsights;
import com.spottrack.platform.analytics.interfaces.rest.resources.RelocationInsightsResource;
import com.spottrack.platform.analytics.interfaces.rest.resources.RelocationInsightsResource.BranchDemandResource;
import com.spottrack.platform.analytics.interfaces.rest.resources.RelocationInsightsResource.RelocationSuggestionResource;
import com.spottrack.platform.analytics.interfaces.rest.resources.RelocationInsightsResource.ThresholdsResource;

public final class RelocationInsightsResourceFromValueAssembler {

    private RelocationInsightsResourceFromValueAssembler() {
    }

    public static RelocationInsightsResource toResourceFromValue(RelocationInsights insights) {
        return new RelocationInsightsResource(insights.gymId(), insights.gymName(), insights.from(), insights.to(),
                new ThresholdsResource(insights.lowDemandRate(), insights.saturatedRate()),
                insights.branches().stream()
                        .map(b -> new BranchDemandResource(b.branchId(), b.branchName(), b.equipmentCount(),
                                b.totalUsageMinutes(), b.utilizationRate(), b.classification().name()))
                        .toList(),
                insights.suggestions().stream()
                        .map(s -> new RelocationSuggestionResource(s.equipmentId(), s.equipmentName(),
                                s.fromBranchId(), s.fromBranchName(), s.fromBranchUtilizationRate(),
                                s.toBranchId(), s.toBranchName(), s.toBranchUtilizationRate(),
                                s.equipmentUtilizationRate(), s.equipmentUsageMinutes(), s.reason()))
                        .toList());
    }
}
