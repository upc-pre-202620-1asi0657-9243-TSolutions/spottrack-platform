package com.spottrack.platform.analytics.application.internal.queryservices;

import com.spottrack.platform.analytics.application.queryservices.UsageAnalyticsQueryService;
import com.spottrack.platform.analytics.domain.model.queries.GetRelocationInsightsQuery;
import com.spottrack.platform.analytics.domain.model.queries.GetUsageHeatmapQuery;
import com.spottrack.platform.analytics.domain.model.valueobjects.BranchDemand;
import com.spottrack.platform.analytics.domain.model.valueobjects.DemandClassification;
import com.spottrack.platform.analytics.domain.model.valueobjects.EquipmentUsage;
import com.spottrack.platform.analytics.domain.model.valueobjects.RelocationInsights;
import com.spottrack.platform.analytics.domain.model.valueobjects.UsageHeatmap;
import com.spottrack.platform.gym.domain.model.aggregates.Equipment;
import com.spottrack.platform.gym.domain.model.entities.Branch;
import com.spottrack.platform.gym.domain.model.valueobjects.EquipmentId;
import com.spottrack.platform.gym.domain.model.valueobjects.EquipmentStatus;
import com.spottrack.platform.gym.domain.model.valueobjects.ZoneId;
import com.spottrack.platform.gym.interfaces.acl.GymContextFacade;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RelocationInsightsQueryServiceImplTest {

    private static final String GYM = "gym";
    private static final LocalDate FROM = LocalDate.of(2026, 10, 1);
    private static final LocalDate TO = LocalDate.of(2026, 10, 28);

    private final UsageAnalyticsQueryService usageService = mock(UsageAnalyticsQueryService.class);
    private final GymContextFacade gymContextFacade = mock(GymContextFacade.class);
    private final List<Branch> branches = new ArrayList<>();
    private RelocationInsightsQueryServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new RelocationInsightsQueryServiceImpl(usageService, gymContextFacade, 20, 50, 10);
        when(gymContextFacade.findBranchesByGymId(GYM)).thenReturn(branches);
        when(gymContextFacade.findGymName(GYM)).thenReturn(Optional.of("SpotTrack"));
    }

    @Test
    void classifiesBranchesAroundTheThresholds() {
        branch("b1", "A-JustBelowLow", 19.9, machine("m1", 10));
        branch("b2", "B-AtLow", 20.0, machine("m2", 10));
        branch("b3", "C-JustBelowSaturated", 49.9, machine("m3", 10));
        branch("b4", "D-AtSaturated", 50.0, machine("m4", 10));

        var insights = run();

        assertEquals(List.of(DemandClassification.LOW_DEMAND, DemandClassification.BALANCED, DemandClassification.BALANCED,
                DemandClassification.SATURATED), insights.branches().stream().map(BranchDemand::classification).toList());
        assertEquals(20.0, insights.lowDemandRate());
        assertEquals(50.0, insights.saturatedRate());
        assertEquals("SpotTrack", insights.gymName());
    }

    @Test
    void reportsEquipmentCountTotalMinutesAndRatePerBranch() {
        branch("b1", "Centro", 33.3, machine("m1", 10, 120.25), machine("m2", 10, 80.5));

        var demand = run().branches().get(0);

        assertEquals(2, demand.equipmentCount());
        assertEquals(200.8, demand.totalUsageMinutes());
        assertEquals(33.3, demand.utilizationRate());
        assertEquals("Centro", demand.branchName());
    }

    @Test
    void gymWithASingleBranchIsClassifiedButNeverGetsSuggestions() {
        branch("b1", "Only", 5.0, machine("m1", 1), machine("m2", 2));

        var insights = run();

        assertEquals(DemandClassification.LOW_DEMAND, insights.branches().get(0).classification());
        assertTrue(insights.suggestions().isEmpty());
    }

    @Test
    void gymWithoutBranchesHasNothingToCompare() {
        var insights = run();

        assertTrue(insights.branches().isEmpty());
        assertTrue(insights.suggestions().isEmpty());
    }

    @Test
    void suggestsTheLeastUsedMachinesOfAnIdleBranchForTheSaturatedOne() {
        branch("idle", "Idle", 8.0, machine("m1", 1.0, 5), machine("m2", 5.0, 30), machine("m3", 25.0, 400), machine("m4", 2.0, 10));
        branch("busy", "Busy", 70.0, machine("m5", 80));

        var suggestions = run().suggestions();

        assertEquals(List.of("m1", "m4"), suggestions.stream().map(s -> s.equipmentId()).toList());
        var first = suggestions.get(0);
        assertEquals("Idle", first.fromBranchName());
        assertEquals("Busy", first.toBranchName());
        assertEquals(8.0, first.fromBranchUtilizationRate());
        assertEquals(70.0, first.toBranchUtilizationRate());
        assertEquals(1.0, first.equipmentUtilizationRate());
        assertEquals(5.0, first.equipmentUsageMinutes());
        assertEquals("Machine m1 was in use 1.0% of opening time at Idle (low demand, 8.0% overall), while Busy runs at 70.0% (saturated)",
                first.reason());
    }

    @Test
    void aBranchWithoutAnyUseIsLowDemandNextToASaturatedOne() {
        branch("dead", "Dead", 0.0, machine("m1", 0), machine("m2", 0));
        branch("busy", "Busy", 90.0, machine("m3", 90));

        var insights = run();

        var dead = insights.branches().stream().filter(b -> b.branchId().equals("dead")).findFirst().orElseThrow();
        assertEquals(DemandClassification.LOW_DEMAND, dead.classification());
        assertEquals(0.0, dead.totalUsageMinutes());
        assertEquals(1, insights.suggestions().size());
    }

    @Test
    void noSaturatedBranchMeansNoSuggestions() {
        branch("a", "A", 0.0, machine("m1", 0), machine("m2", 0));
        branch("b", "B", 10.0, machine("m3", 1), machine("m4", 1));

        var insights = run();

        assertTrue(insights.branches().stream().allMatch(b -> b.classification() == DemandClassification.LOW_DEMAND));
        assertTrue(insights.suggestions().isEmpty());
    }

    @Test
    void aBranchWithoutEquipmentIsBalancedAndNeitherGivesNorReceives() {
        branch("empty", "Empty", 0.0);
        branch("busy", "Busy", 80.0, machine("m1", 80));

        var insights = run();

        var empty = insights.branches().stream().filter(b -> b.branchId().equals("empty")).findFirst().orElseThrow();
        assertEquals(0, empty.equipmentCount());
        assertEquals(DemandClassification.BALANCED, empty.classification());
        assertTrue(insights.suggestions().isEmpty());
    }

    @Test
    void machinesOutOfServiceInMaintenanceOrBusyEnoughAreNotCandidates() {
        branch("idle", "Idle", 5.0,
                machine("broken", 0, 0, EquipmentStatus.OUT_OF_SERVICE),
                machine("repair", 0, 0, EquipmentStatus.MAINTENANCE),
                machine("popular", 20.0, 300, EquipmentStatus.AVAILABLE),
                machine("ok", 3.0, 20, EquipmentStatus.AVAILABLE),
                machine("ok2", 4.0, 30, EquipmentStatus.OCCUPIED),
                machine("ok3", 4.5, 31, EquipmentStatus.RESERVED));
        branch("busy", "Busy", 75.0, machine("x", 75));

        var ids = run().suggestions().stream().map(s -> s.equipmentId()).toList();

        assertEquals(List.of("ok", "ok2", "ok3"), ids);
    }

    @Test
    void aBranchNeverGivesAwayMoreThanHalfOfItsMachines() {
        branch("one", "One", 1.0, machine("m1", 0));
        branch("three", "Three", 2.0, machine("m2", 0), machine("m3", 1), machine("m4", 2));
        branch("busy", "Busy", 90.0, machine("m5", 90));

        var suggestions = run().suggestions();

        assertEquals(List.of("m2"), suggestions.stream().map(s -> s.equipmentId()).toList());
    }

    @Test
    void capsTheTotalNumberOfSuggestionsKeepingTheLeastUsedFirst() {
        service = new RelocationInsightsQueryServiceImpl(usageService, gymContextFacade, 20, 50, 2);
        branch("a", "A", 5.0, machine("a1", 4), machine("a2", 3), machine("a3", 2), machine("a4", 1));
        branch("b", "B", 6.0, machine("b1", 0), machine("b2", 9));
        branch("busy", "Busy", 90.0, machine("x", 90));

        var suggestions = run().suggestions();

        assertEquals(List.of("b1", "a4"), suggestions.stream().map(s -> s.equipmentId()).toList());
    }

    @Test
    void spreadsSuggestionsOverSaturatedBranchesStartingWithTheMostSaturated() {
        branch("idle", "Idle", 5.0, machine("m1", 1), machine("m2", 2), machine("m3", 3), machine("m4", 4), machine("m5", 5), machine("m6", 6));
        branch("beta", "Beta", 60.0, machine("x1", 60));
        branch("gamma", "Gamma", 85.0, machine("x2", 85));

        var targets = run().suggestions().stream().map(s -> s.toBranchName()).toList();

        assertEquals(List.of("Gamma", "Beta", "Gamma"), targets);
    }

    @Test
    void tiesAreBrokenDeterministicallyByNameAndEquipmentId() {
        branch("s1", "Zeta", 60.0, machine("x1", 60));
        branch("s2", "Alfa", 60.0, machine("x2", 60));
        branch("i1", "IdleOne", 5.0, machine("m2", 3, 10), machine("m1", 3, 10), machine("m3", 9));
        branch("i2", "IdleTwo", 5.0, machine("m4", 3, 10), machine("m5", 3, 10), machine("m6", 9));

        var suggestions = run().suggestions();

        assertEquals(List.of("m1", "m4"), suggestions.stream().map(s -> s.equipmentId()).toList());
        assertEquals(List.of("Alfa", "Zeta"), suggestions.stream().map(s -> s.toBranchName()).toList());
    }

    @Test
    void usesTheConfiguredThresholds() {
        service = new RelocationInsightsQueryServiceImpl(usageService, gymContextFacade, 40, 45, 10);
        branch("a", "A", 39.0, machine("m1", 30), machine("m2", 50));
        branch("b", "B", 45.0, machine("m3", 50));

        var insights = run();

        assertEquals(DemandClassification.LOW_DEMAND, insights.branches().get(0).classification());
        assertEquals(DemandClassification.SATURATED, insights.branches().get(1).classification());
        assertEquals(List.of("m1"), insights.suggestions().stream().map(s -> s.equipmentId()).toList());
        assertEquals(40.0, insights.lowDemandRate());
    }

    @Test
    void readsTheHeatmapOfEachBranchOverTheSameWindow() {
        branch("a", "A", 10.0, machine("m1", 1), machine("m2", 1));
        branch("b", "B", 60.0, machine("m3", 60));

        run();

        verify(usageService).handle(new GetUsageHeatmapQuery(GYM, "a", FROM, TO));
        verify(usageService).handle(new GetUsageHeatmapQuery(GYM, "b", FROM, TO));
    }

    @Test
    void rejectsInconsistentConfiguration() {
        assertThrows(IllegalArgumentException.class, () -> new RelocationInsightsQueryServiceImpl(usageService, gymContextFacade, 50, 50, 10));
        assertThrows(IllegalArgumentException.class, () -> new RelocationInsightsQueryServiceImpl(usageService, gymContextFacade, 60, 40, 10));
        assertThrows(IllegalArgumentException.class, () -> new RelocationInsightsQueryServiceImpl(usageService, gymContextFacade, -1, 50, 10));
        assertThrows(IllegalArgumentException.class, () -> new RelocationInsightsQueryServiceImpl(usageService, gymContextFacade, 20, 101, 10));
        assertThrows(IllegalArgumentException.class, () -> new RelocationInsightsQueryServiceImpl(usageService, gymContextFacade, 20, 50, -1));
    }

    private RelocationInsights run() {
        return service.handle(new GetRelocationInsightsQuery(GYM, FROM, TO));
    }

    private void branch(String id, String name, double rate, Machine... machines) {
        branches.add(new Branch(id, GYM, name, "address"));
        var equipment = new ArrayList<Equipment>();
        var usage = new ArrayList<EquipmentUsage>();
        for (var m : machines) {
            equipment.add(equipment(m));
            usage.add(new EquipmentUsage(m.id(), "zone", m.minutes(), 1, m.utilization(), 0));
        }
        equipment.add(equipment(new Machine("decommissioned-" + id, 0, 0, EquipmentStatus.DECOMMISSIONED)));
        when(gymContextFacade.findEquipmentsByBranchId(id)).thenReturn(equipment);
        when(usageService.handle(new GetUsageHeatmapQuery(GYM, id, FROM, TO))).thenReturn(
                new UsageHeatmap(FROM, TO, 6, 22, machines.length, rate, 0, null, 0, List.of(), usage));
    }

    private static Machine machine(String id, double utilization) {
        return new Machine(id, utilization, 0, EquipmentStatus.AVAILABLE);
    }

    private static Machine machine(String id, double utilization, double minutes) {
        return new Machine(id, utilization, minutes, EquipmentStatus.AVAILABLE);
    }

    private static Machine machine(String id, double utilization, double minutes, EquipmentStatus status) {
        return new Machine(id, utilization, minutes, status);
    }

    private static Equipment equipment(Machine m) {
        var equipment = new Equipment();
        equipment.setId(new EquipmentId(m.id()));
        equipment.setEquipmentName("Machine " + m.id());
        equipment.setStatus(m.status());
        equipment.setZoneId(new ZoneId("zone"));
        return equipment;
    }

    private record Machine(String id, double utilization, double minutes, EquipmentStatus status) {
    }
}
