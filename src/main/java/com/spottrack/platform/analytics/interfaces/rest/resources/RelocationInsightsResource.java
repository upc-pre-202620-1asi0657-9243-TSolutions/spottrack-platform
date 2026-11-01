package com.spottrack.platform.analytics.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.util.List;

public record RelocationInsightsResource(String gymId, String gymName, LocalDate from, LocalDate to,
                                         ThresholdsResource thresholds,
                                         List<BranchDemandResource> branches,
                                         List<RelocationSuggestionResource> suggestions) {

    public record ThresholdsResource(
            @Schema(description = "Branches below this utilization % are LOW_DEMAND") double lowDemandRate,
            @Schema(description = "Branches at or above this utilization % are SATURATED") double saturatedRate) {
    }

    public record BranchDemandResource(String branchId, String name,
                                       @Schema(description = "Equipment of the branch, decommissioned ones excluded") int equipmentCount,
                                       @Schema(description = "Minutes of real use of the branch's equipment in the window") double totalUsageMinutes,
                                       @Schema(description = "Average % of the branch's equipment in use during opening hours") double utilizationRate,
                                       @Schema(description = "LOW_DEMAND, BALANCED or SATURATED", allowableValues = {"LOW_DEMAND", "BALANCED", "SATURATED"}) String classification) {
    }

    public record RelocationSuggestionResource(String equipmentId, String equipmentName,
                                               String fromBranchId, String fromBranchName, double fromBranchUtilizationRate,
                                               String toBranchId, String toBranchName, double toBranchUtilizationRate,
                                               @Schema(description = "% of the window's opening hours this machine was in use") double equipmentUtilizationRate,
                                               @Schema(description = "Minutes of real use of this machine in the window") double equipmentUsageMinutes,
                                               @Schema(description = "Human-readable explanation with the figures behind the suggestion") String reason) {
    }
}
