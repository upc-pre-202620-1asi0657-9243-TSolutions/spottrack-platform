package com.spottrack.platform.analytics.interfaces.rest.transform;

import com.spottrack.platform.analytics.domain.model.valueobjects.UsageHeatmap;
import com.spottrack.platform.analytics.interfaces.rest.resources.UsageHeatmapResource;
import com.spottrack.platform.analytics.interfaces.rest.resources.UsageHeatmapResource.EquipmentUsageResource;
import com.spottrack.platform.analytics.interfaces.rest.resources.UsageHeatmapResource.HourlyOccupancyResource;

public final class UsageHeatmapResourceFromValueAssembler {

    private UsageHeatmapResourceFromValueAssembler() {
    }

    public static UsageHeatmapResource toResourceFromValue(UsageHeatmap heatmap) {
        return new UsageHeatmapResource(heatmap.from(), heatmap.to(), heatmap.openingHour(), heatmap.closingHour(),
                heatmap.equipmentCount(), heatmap.occupancyRate(), heatmap.previousOccupancyRate(),
                heatmap.peakHour(), heatmap.peakOccupancyRate(),
                heatmap.hourly().stream()
                        .map(h -> new HourlyOccupancyResource(h.hour(), h.occupancyRate()))
                        .toList(),
                heatmap.equipment().stream()
                        .map(e -> new EquipmentUsageResource(e.equipmentId(), e.zoneId(), e.activeMinutes(),
                                e.sessionCount(), e.utilizationRate(), e.heat()))
                        .toList());
    }
}
