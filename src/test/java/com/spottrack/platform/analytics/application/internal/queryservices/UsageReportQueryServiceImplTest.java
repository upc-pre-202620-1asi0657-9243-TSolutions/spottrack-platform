package com.spottrack.platform.analytics.application.internal.queryservices;

import com.spottrack.platform.analytics.application.queryservices.UsageAnalyticsQueryService;
import com.spottrack.platform.analytics.domain.model.queries.GetUsageHeatmapQuery;
import com.spottrack.platform.analytics.domain.model.queries.GetUsageReportQuery;
import com.spottrack.platform.analytics.domain.model.valueobjects.UsageHeatmap;
import com.spottrack.platform.gym.domain.model.aggregates.Equipment;
import com.spottrack.platform.gym.domain.model.valueobjects.EquipmentStatus;
import com.spottrack.platform.gym.domain.model.valueobjects.ManufacturerId;
import com.spottrack.platform.gym.domain.model.valueobjects.ZoneId;
import com.spottrack.platform.gym.interfaces.acl.GymContextFacade;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UsageReportQueryServiceImplTest {

    private static final LocalDate FROM = LocalDate.of(2026, 10, 1);
    private static final LocalDate TO = LocalDate.of(2026, 10, 7);

    private UsageAnalyticsQueryService usageService;
    private GymContextFacade gymContextFacade;
    private UsageReportQueryServiceImpl service;
    private UsageHeatmap heatmap;
    private Equipment rower;

    @BeforeEach
    void setUp() {
        usageService = mock(UsageAnalyticsQueryService.class);
        gymContextFacade = mock(GymContextFacade.class);
        service = new UsageReportQueryServiceImpl(usageService, gymContextFacade);
        heatmap = new UsageHeatmap(FROM, TO, 6, 22, 1, 10, 5, null, 0, List.of(), List.of());
        when(usageService.handle(any(GetUsageHeatmapQuery.class))).thenReturn(heatmap);
        rower = new Equipment(EquipmentStatus.AVAILABLE, "Remo", "R-1", new ManufacturerId("m-1"),
                BigDecimal.TEN, "USD", new ZoneId("zone-1"));
    }

    @Test
    void gymReportUsesTheGymHeatmapAndNamesGymAndEquipment() {
        when(gymContextFacade.findEquipmentsByGymId("gym-1")).thenReturn(List.of(rower));
        when(gymContextFacade.findGymName("gym-1")).thenReturn(Optional.of("Gym Central"));

        var report = service.handle(new GetUsageReportQuery("gym-1", null, FROM, TO));

        var captor = ArgumentCaptor.forClass(GetUsageHeatmapQuery.class);
        verify(usageService).handle(captor.capture());
        assertEquals("gym-1", captor.getValue().gymId());
        assertNull(captor.getValue().branchId());
        assertSame(heatmap, report.heatmap());
        assertEquals("Gym Central", report.gymName());
        assertNull(report.branchName());
        assertEquals("Remo", report.equipmentNames().get(rower.getId().uuid()));
        verify(gymContextFacade, never()).findBranchName(any());
        verify(gymContextFacade, never()).findEquipmentsByBranchId(any());
    }

    @Test
    void branchReportNamesTheBranchAndOnlyLooksUpItsEquipment() {
        when(gymContextFacade.findEquipmentsByBranchId("branch-1")).thenReturn(List.of(rower));
        when(gymContextFacade.findGymName("gym-1")).thenReturn(Optional.of("Gym Central"));
        when(gymContextFacade.findBranchName("branch-1")).thenReturn(Optional.of("San Isidro"));

        var report = service.handle(new GetUsageReportQuery("gym-1", "branch-1", FROM, TO));

        var captor = ArgumentCaptor.forClass(GetUsageHeatmapQuery.class);
        verify(usageService).handle(captor.capture());
        assertEquals("branch-1", captor.getValue().branchId());
        assertEquals("San Isidro", report.branchName());
        assertEquals("Gym Central", report.gymName());
        verify(gymContextFacade, never()).findEquipmentsByGymId(any());
    }

    @Test
    void unknownNamesStayNullInsteadOfFailing() {
        when(gymContextFacade.findGymName("gym-1")).thenReturn(Optional.empty());
        when(gymContextFacade.findBranchName("branch-1")).thenReturn(Optional.empty());

        var report = service.handle(new GetUsageReportQuery("gym-1", "branch-1", FROM, TO));

        assertNull(report.gymName());
        assertNull(report.branchName());
        assertEquals(0, report.equipmentNames().size());
    }

    @Test
    void queryRejectsAnInvertedRange() {
        assertThrows(IllegalArgumentException.class, () -> new GetUsageReportQuery("gym-1", null, TO, FROM));
    }
}
