package com.spottrack.platform.analytics.domain.model.valueobjects;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Usage heatmap plus the names needed to present it as a formal document.
 *
 * @param branchName     null when the report covers the whole gym
 * @param equipmentNames equipment name by equipment id
 */
public record UsageReport(String gymName, String branchName, LocalDateTime generatedAt,
                          UsageHeatmap heatmap, Map<String, String> equipmentNames) {
}
