package com.spottrack.platform.analytics.interfaces.rest.resources;

import java.time.LocalDate;
import java.util.List;

public record RelocationInsightsResource(String gymId, String gymName, LocalDate from, LocalDate to,
                                         ThresholdsResource thresholds,
                                         List<BranchDemandResource> branches,
                                         List<RelocationSuggestionResource> suggestions) {

    public record ThresholdsResource(double lowDemandRate, double saturatedRate) {
    }

    public record BranchDemandResource(String branchId, String name, int equipmentCount, double totalUsageMinutes,
                                       double utilizationRate, String classification) {
    }

    public record RelocationSuggestionResource(String equipmentId, String equipmentName,
                                               String fromBranchId, String fromBranchName, double fromBranchUtilizationRate,
                                               String toBranchId, String toBranchName, double toBranchUtilizationRate,
                                               double equipmentUtilizationRate, double equipmentUsageMinutes,
                                               String reason) {
    }
}
