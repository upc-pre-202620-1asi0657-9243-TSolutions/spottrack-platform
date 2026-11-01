package com.spottrack.platform.analytics.domain.model.valueobjects;

/**
 * @param totalUsageMinutes minutes of real use of the branch's equipment in the window
 * @param utilizationRate   average % of the branch's equipment in use during opening hours, comparable across branches of any size
 */
public record BranchDemand(String branchId, String branchName, int equipmentCount, double totalUsageMinutes,
                           double utilizationRate, DemandClassification classification) {
}
