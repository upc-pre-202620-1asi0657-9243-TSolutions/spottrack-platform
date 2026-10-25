package com.spottrack.platform.analytics.application.internal.queryservices;

import com.spottrack.platform.analytics.domain.model.aggregates.UsageSession;
import com.spottrack.platform.analytics.domain.model.queries.GetUsageHeatmapQuery;
import com.spottrack.platform.analytics.domain.model.valueobjects.EquipmentUsage;
import com.spottrack.platform.analytics.domain.model.valueobjects.HourlyOccupancy;
import com.spottrack.platform.analytics.domain.repositories.UsageSessionRepository;
import com.spottrack.platform.gym.domain.model.aggregates.Equipment;
import com.spottrack.platform.gym.domain.model.valueobjects.EquipmentId;
import com.spottrack.platform.gym.domain.model.valueobjects.EquipmentStatus;
import com.spottrack.platform.gym.domain.model.valueobjects.ZoneId;
import com.spottrack.platform.gym.interfaces.acl.GymContextFacade;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UsageAnalyticsQueryServiceImplTest {

    private static final LocalDate DAY = LocalDate.of(2026, 1, 5);

    private final List<UsageSession> sessions = new ArrayList<>();
    private final GymContextFacade gymContextFacade = mock(GymContextFacade.class);
    private UsageAnalyticsQueryServiceImpl service;

    @BeforeEach
    void setUp() {
        UsageSessionRepository repository = new InMemoryUsageSessionRepository(sessions);
        service = new UsageAnalyticsQueryServiceImpl(repository, gymContextFacade, 6, 22);
        when(gymContextFacade.findEquipmentsByGymId("gym")).thenReturn(List.of(
                equipment("A", EquipmentStatus.AVAILABLE),
                equipment("B", EquipmentStatus.OCCUPIED),
                equipment("C", EquipmentStatus.DECOMMISSIONED)));
    }

    @Test
    void computesOccupancyHeatAndPeakFromSessionHistory() {
        session("A", DAY.atTime(8, 0), DAY.atTime(9, 0));
        session("B", DAY.atTime(8, 30), DAY.atTime(9, 30));
        session("A", DAY.atTime(23, 0), DAY.atTime(23, 30)); // after closing: usage, not occupancy
        session("B", DAY.minusDays(1).atTime(10, 0), DAY.minusDays(1).atTime(11, 0)); // previous window

        var heatmap = service.handle(new GetUsageHeatmapQuery("gym", null, DAY, DAY));

        assertEquals(2, heatmap.equipmentCount(), "decommissioned equipment is left out");
        // 120 open-hour minutes / (2 machines × 16 h × 60 min)
        assertEquals(6.3, heatmap.occupancyRate());
        assertEquals(3.1, heatmap.previousOccupancyRate());
        assertEquals(8, heatmap.peakHour());
        assertEquals(75.0, heatmap.peakOccupancyRate());
        assertEquals(new HourlyOccupancy(9, 25.0), heatmap.hourly().get(3));
        assertEquals(16, heatmap.hourly().size());

        var a = usageOf(heatmap.equipment(), "A");
        var b = usageOf(heatmap.equipment(), "B");
        assertEquals(90.0, a.activeMinutes());
        assertEquals(2, a.sessionCount());
        assertEquals(9.4, a.utilizationRate());
        assertEquals(1.0, a.heat());
        assertEquals(0.667, b.heat());
    }

    @Test
    void emptyHistoryHasNoPeakAndColdHeat() {
        var heatmap = service.handle(new GetUsageHeatmapQuery("gym", null, DAY, DAY));

        assertEquals(0.0, heatmap.occupancyRate());
        assertNull(heatmap.peakHour());
        heatmap.equipment().forEach(e -> assertEquals(0.0, e.heat()));
    }

    private void session(String equipmentId, LocalDateTime start, LocalDateTime end) {
        sessions.add(new UsageSession(null, equipmentId, start, end));
    }

    private static Equipment equipment(String id, EquipmentStatus status) {
        var equipment = new Equipment();
        equipment.setId(new EquipmentId(id));
        equipment.setStatus(status);
        equipment.setZoneId(new ZoneId("zone"));
        return equipment;
    }

    private static EquipmentUsage usageOf(List<EquipmentUsage> usage, String equipmentId) {
        return usage.stream().filter(u -> u.equipmentId().equals(equipmentId)).findFirst().orElseThrow();
    }

    private record InMemoryUsageSessionRepository(List<UsageSession> sessions) implements UsageSessionRepository {
        public UsageSession save(UsageSession usageSession) { sessions.add(usageSession); return usageSession; }
        public List<UsageSession> saveAll(List<UsageSession> usageSessions) { sessions.addAll(usageSessions); return usageSessions; }
        public boolean existsByEquipmentId(String equipmentId) {
            return sessions.stream().anyMatch(s -> s.getEquipmentId().equals(equipmentId));
        }
        public List<UsageSession> findOverlapping(Collection<String> equipmentIds, LocalDateTime from, LocalDateTime to) {
            return sessions.stream()
                    .filter(s -> equipmentIds.contains(s.getEquipmentId()))
                    .filter(s -> s.getStartedAt().isBefore(to) && s.getEndedAt().isAfter(from))
                    .toList();
        }
    }
}
