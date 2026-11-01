package com.spottrack.platform.analytics.domain.model.valueobjects;

/**
 * Move one under-used machine of a low-demand branch to a saturated one.
 *
 * @param equipmentUtilizationRate % of the window's opening hours this machine was in use
 */
public record RelocationSuggestion(String equipmentId, String equipmentName,
                                   String fromBranchId, String fromBranchName, double fromBranchUtilizationRate,
                                   String toBranchId, String toBranchName, double toBranchUtilizationRate,
                                   double equipmentUtilizationRate, double equipmentUsageMinutes, String reason) {
}
