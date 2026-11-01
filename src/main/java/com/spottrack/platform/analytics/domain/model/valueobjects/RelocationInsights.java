package com.spottrack.platform.analytics.domain.model.valueobjects;

import java.time.LocalDate;
import java.util.List;

/**
 * @param lowDemandRate  branches below this utilization % are LOW_DEMAND
 * @param saturatedRate  branches at or above this utilization % are SATURATED
 */
public record RelocationInsights(String gymId, String gymName, LocalDate from, LocalDate to,
                                 double lowDemandRate, double saturatedRate,
                                 List<BranchDemand> branches, List<RelocationSuggestion> suggestions) {
}
