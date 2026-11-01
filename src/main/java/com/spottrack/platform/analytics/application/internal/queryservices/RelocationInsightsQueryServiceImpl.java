package com.spottrack.platform.analytics.application.internal.queryservices;

import com.spottrack.platform.analytics.application.queryservices.RelocationInsightsQueryService;
import com.spottrack.platform.analytics.application.queryservices.UsageAnalyticsQueryService;
import com.spottrack.platform.analytics.domain.model.queries.GetRelocationInsightsQuery;
import com.spottrack.platform.analytics.domain.model.queries.GetUsageHeatmapQuery;
import com.spottrack.platform.analytics.domain.model.valueobjects.BranchDemand;
import com.spottrack.platform.analytics.domain.model.valueobjects.DemandClassification;
import com.spottrack.platform.analytics.domain.model.valueobjects.EquipmentUsage;
import com.spottrack.platform.analytics.domain.model.valueobjects.RelocationInsights;
import com.spottrack.platform.analytics.domain.model.valueobjects.RelocationSuggestion;
import com.spottrack.platform.analytics.domain.model.valueobjects.UsageHeatmap;
import com.spottrack.platform.gym.domain.model.aggregates.Equipment;
import com.spottrack.platform.gym.domain.model.entities.Branch;
import com.spottrack.platform.gym.domain.model.valueobjects.EquipmentStatus;
import com.spottrack.platform.gym.interfaces.acl.GymContextFacade;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Compares the demand of the branches of a gym and suggests moving machines out of idle branches.
 * <ul>
 *   <li>A branch's rate is its heatmap occupancy: % of its machines in use during opening hours.
 *       It is per machine, so branches of different size are comparable.</li>
 *   <li>Below {@code low-demand-rate} a branch is LOW_DEMAND, at or above {@code saturated-rate}
 *       it is SATURATED, otherwise BALANCED. A branch without equipment is BALANCED.</li>
 *   <li>Candidates are the least-used machines of LOW_DEMAND branches whose own utilization is also
 *       below {@code low-demand-rate}. Machines out of service or in maintenance are not candidates.</li>
 *   <li>A branch never gives away more than half of its machines, and at most {@code max-suggestions}
 *       are returned: the least-used first, spread over the saturated branches (most saturated first).</li>
 * </ul>
 * Usage is attributed to the branch where a machine is now, so history follows a machine that was relocated.
 */
@Service
public class RelocationInsightsQueryServiceImpl implements RelocationInsightsQueryService {

    private final UsageAnalyticsQueryService usageAnalyticsQueryService;
    private final GymContextFacade gymContextFacade;
    private final double lowDemandRate;
    private final double saturatedRate;
    private final int maxSuggestions;

    public RelocationInsightsQueryServiceImpl(UsageAnalyticsQueryService usageAnalyticsQueryService,
                                              GymContextFacade gymContextFacade,
                                              @Value("${spottrack.analytics.relocation.low-demand-rate:20}") double lowDemandRate,
                                              @Value("${spottrack.analytics.relocation.saturated-rate:50}") double saturatedRate,
                                              @Value("${spottrack.analytics.relocation.max-suggestions:10}") int maxSuggestions) {
        if (lowDemandRate < 0 || saturatedRate > 100 || lowDemandRate >= saturatedRate) {
            throw new IllegalArgumentException("Relocation thresholds must satisfy 0 <= low-demand-rate < saturated-rate <= 100");
        }
        if (maxSuggestions < 0) {
            throw new IllegalArgumentException("max-suggestions must not be negative");
        }
        this.usageAnalyticsQueryService = usageAnalyticsQueryService;
        this.gymContextFacade = gymContextFacade;
        this.lowDemandRate = lowDemandRate;
        this.saturatedRate = saturatedRate;
        this.maxSuggestions = maxSuggestions;
    }

    @Override
    public RelocationInsights handle(GetRelocationInsightsQuery query) {
        var profiles = gymContextFacade.findBranchesByGymId(query.gymId()).stream()
                .sorted(Comparator.comparing(Branch::getName, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(b -> b.getId().uuid()))
                .map(branch -> profile(query, branch))
                .toList();

        var branches = profiles.stream().map(BranchProfile::demand).toList();
        var gymName = gymContextFacade.findGymName(query.gymId()).orElse(null);
        return new RelocationInsights(query.gymId(), gymName, query.from(), query.to(), lowDemandRate, saturatedRate,
                branches, suggestions(profiles));
    }

    private BranchProfile profile(GetRelocationInsightsQuery query, Branch branch) {
        var branchId = branch.getId().uuid();
        var heatmap = usageAnalyticsQueryService.handle(
                new GetUsageHeatmapQuery(query.gymId(), branchId, query.from(), query.to()));
        Map<String, Equipment> equipment = new HashMap<>();
        for (var e : gymContextFacade.findEquipmentsByBranchId(branchId)) {
            equipment.put(e.getId().uuid(), e);
        }
        var totalMinutes = heatmap.equipment().stream().mapToDouble(EquipmentUsage::activeMinutes).sum();
        var demand = new BranchDemand(branchId, branch.getName(), heatmap.equipmentCount(), round(totalMinutes),
                heatmap.occupancyRate(), classify(heatmap));
        return new BranchProfile(demand, heatmap, equipment);
    }

    private DemandClassification classify(UsageHeatmap heatmap) {
        if (heatmap.equipmentCount() == 0) return DemandClassification.BALANCED;
        if (heatmap.occupancyRate() < lowDemandRate) return DemandClassification.LOW_DEMAND;
        if (heatmap.occupancyRate() >= saturatedRate) return DemandClassification.SATURATED;
        return DemandClassification.BALANCED;
    }

    private List<RelocationSuggestion> suggestions(List<BranchProfile> profiles) {
        var targets = profiles.stream()
                .filter(p -> p.demand().classification() == DemandClassification.SATURATED)
                .sorted(Comparator.comparingDouble((BranchProfile p) -> p.demand().utilizationRate()).reversed()
                        .thenComparing(p -> p.demand().branchName(), Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(p -> p.demand().branchId()))
                .map(BranchProfile::demand)
                .toList();
        if (targets.isEmpty() || maxSuggestions == 0) return List.of();

        var candidates = new ArrayList<Candidate>();
        for (var source : profiles) {
            if (source.demand().classification() != DemandClassification.LOW_DEMAND) continue;
            source.heatmap().equipment().stream()
                    .filter(usage -> usage.utilizationRate() < lowDemandRate)
                    .filter(usage -> isMovable(source.equipment().get(usage.equipmentId())))
                    .sorted(Comparator.comparingDouble(EquipmentUsage::utilizationRate)
                            .thenComparingDouble(EquipmentUsage::activeMinutes)
                            .thenComparing(EquipmentUsage::equipmentId))
                    .limit(source.demand().equipmentCount() / 2)
                    .forEach(usage -> candidates.add(
                            new Candidate(source.demand(), usage, source.equipment().get(usage.equipmentId()))));
        }
        candidates.sort(Comparator.comparingDouble((Candidate c) -> c.usage().utilizationRate())
                .thenComparingDouble(c -> c.usage().activeMinutes())
                .thenComparing(c -> c.usage().equipmentId()));

        var suggestions = new ArrayList<RelocationSuggestion>();
        for (var candidate : candidates.stream().limit(maxSuggestions).toList()) {
            var target = targets.get(suggestions.size() % targets.size());
            suggestions.add(suggestion(candidate, target));
        }
        return suggestions;
    }

    private static boolean isMovable(Equipment equipment) {
        return equipment != null
                && equipment.getStatus() != EquipmentStatus.OUT_OF_SERVICE
                && equipment.getStatus() != EquipmentStatus.MAINTENANCE;
    }

    private static RelocationSuggestion suggestion(Candidate candidate, BranchDemand target) {
        var source = candidate.source();
        var usage = candidate.usage();
        var name = candidate.equipment().getEquipmentName();
        var reason = String.format(Locale.ROOT,
                "%s was in use %.1f%% of opening time at %s (low demand, %.1f%% overall), while %s runs at %.1f%% (saturated)",
                name, usage.utilizationRate(), source.branchName(), source.utilizationRate(),
                target.branchName(), target.utilizationRate());
        return new RelocationSuggestion(usage.equipmentId(), name,
                source.branchId(), source.branchName(), source.utilizationRate(),
                target.branchId(), target.branchName(), target.utilizationRate(),
                usage.utilizationRate(), usage.activeMinutes(), reason);
    }

    private static double round(double value) {
        return Math.round(value * 10) / 10.0;
    }

    private record BranchProfile(BranchDemand demand, UsageHeatmap heatmap, Map<String, Equipment> equipment) {
    }

    private record Candidate(BranchDemand source, EquipmentUsage usage, Equipment equipment) {
    }
}
