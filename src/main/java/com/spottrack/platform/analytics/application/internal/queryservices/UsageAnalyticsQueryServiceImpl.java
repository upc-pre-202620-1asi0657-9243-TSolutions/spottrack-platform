package com.spottrack.platform.analytics.application.internal.queryservices;

import com.spottrack.platform.analytics.application.queryservices.UsageAnalyticsQueryService;
import com.spottrack.platform.analytics.domain.model.aggregates.UsageSession;
import com.spottrack.platform.analytics.domain.model.queries.GetUsageHeatmapQuery;
import com.spottrack.platform.analytics.domain.model.valueobjects.DailyUsage;
import com.spottrack.platform.analytics.domain.model.valueobjects.DayHourOccupancy;
import com.spottrack.platform.analytics.domain.model.valueobjects.EquipmentUsage;
import com.spottrack.platform.analytics.domain.model.valueobjects.HourlyOccupancy;
import com.spottrack.platform.analytics.domain.model.valueobjects.UsageHeatmap;
import com.spottrack.platform.analytics.domain.repositories.UsageSessionRepository;
import com.spottrack.platform.gym.interfaces.acl.EquipmentSnapshot;
import com.spottrack.platform.shared.domain.model.valueobjects.EquipmentStatus;
import com.spottrack.platform.gym.interfaces.acl.GymContextFacade;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * Turns UsageSession history into occupancy figures.
 * <ul>
 *   <li>Capacity = equipment count × opening minutes that have already elapsed in the window.</li>
 *   <li>Occupancy % = minutes of use inside opening hours ÷ capacity.</li>
 *   <li>Hourly occupancy = the same ratio restricted to one hour of the day across every day.</li>
 *   <li>Hourly occupancy by day = the same ratio restricted to one hour of one day of the week.</li>
 *   <li>Heat = an equipment's minutes of use ÷ the busiest equipment's, so the map always
 *       spreads from cold to hot within the selected scope.</li>
 *   <li>Daily usage = minutes of use per day of the week, for the window and the one before it.</li>
 *   <li>Inactive equipment = equipment with no minutes of use in the window (and in the one before it).</li>
 * </ul>
 * Decommissioned equipment is left out: it can't be used, so it would only dilute the rates.
 */
@Service
public class UsageAnalyticsQueryServiceImpl implements UsageAnalyticsQueryService {

    private final UsageSessionRepository usageSessionRepository;
    private final GymContextFacade gymContextFacade;
    private final int openingHour;
    private final int closingHour;

    public UsageAnalyticsQueryServiceImpl(UsageSessionRepository usageSessionRepository,
                                          GymContextFacade gymContextFacade,
                                          @Value("${spottrack.analytics.opening-hour:6}") int openingHour,
                                          @Value("${spottrack.analytics.closing-hour:22}") int closingHour) {
        if (openingHour < 0 || closingHour > 24 || openingHour >= closingHour) {
            throw new IllegalArgumentException("Opening hours must satisfy 0 <= opening < closing <= 24");
        }
        this.usageSessionRepository = usageSessionRepository;
        this.gymContextFacade = gymContextFacade;
        this.openingHour = openingHour;
        this.closingHour = closingHour;
    }

    @Override
    public UsageHeatmap handle(GetUsageHeatmapQuery query) {
        var equipment = (query.branchId() != null
                ? gymContextFacade.findEquipmentSnapshotsByBranchId(query.branchId())
                : gymContextFacade.findEquipmentSnapshotsByGymId(query.gymId())).stream()
                .filter(e -> e.status() != EquipmentStatus.DECOMMISSIONED)
                .toList();
        var equipmentIds = equipment.stream().map(e -> e.equipmentId()).toList();

        var now = LocalDateTime.now();
        var from = query.from().atStartOfDay();
        var to = query.to().plusDays(1).atStartOfDay();
        var windowDays = ChronoUnit.DAYS.between(query.from(), query.to().plusDays(1));
        var previousFrom = from.minusDays(windowDays);

        // One query covers both the window and the one before it.
        var sessions = usageSessionRepository.findOverlapping(equipmentIds, previousFrom, min(to, now));
        var current = sessions.stream().filter(s -> s.getEndedAt().isAfter(from)).toList();

        var hourly = hourlyOccupancy(current, equipment.size(), from, min(to, now));
        var occupancy = openHoursOccupancy(current, equipment.size(), from, min(to, now));
        var previousOccupancy = openHoursOccupancy(sessions, equipment.size(), previousFrom, min(from, now));

        var peak = hourly.stream()
                .filter(h -> h.occupancyRate() > 0)
                .max((a, b) -> Double.compare(a.occupancyRate(), b.occupancyRate()));

        var usage = equipmentUsage(equipment, current, from, min(to, now));
        var inactive = (int) usage.stream().filter(u -> u.activeMinutes() == 0).count();
        var previousInactive = inactiveCount(equipmentIds, sessions, previousFrom, min(from, now));
        return new UsageHeatmap(query.from(), query.to(), openingHour, closingHour, equipment.size(),
                occupancy, previousOccupancy,
                peak.map(HourlyOccupancy::hour).orElse(null), peak.map(HourlyOccupancy::occupancyRate).orElse(0.0),
                hourly, usage,
                dailyUsage(sessions, from, min(to, now), previousFrom, min(from, now)),
                inactive, previousInactive,
                hourlyOccupancyByDay(current, equipment.size(), from, min(to, now)));
    }

    private static List<DailyUsage> dailyUsage(List<UsageSession> sessions, LocalDateTime from, LocalDateTime to,
                                               LocalDateTime previousFrom, LocalDateTime previousTo) {
        var current = minutesByDayOfWeek(sessions, from, to);
        var previous = minutesByDayOfWeek(sessions, previousFrom, previousTo);
        var result = new ArrayList<DailyUsage>();
        for (var day : DayOfWeek.values()) {
            result.add(new DailyUsage(day, round(current[day.ordinal()]), round(previous[day.ordinal()])));
        }
        return result;
    }

    /** Minutes of use inside [from, to), split by the day of the week they happened on (index = DayOfWeek.ordinal). */
    private static double[] minutesByDayOfWeek(List<UsageSession> sessions, LocalDateTime from, LocalDateTime to) {
        var minutes = new double[7];
        if (!to.isAfter(from)) return minutes;
        for (var session : sessions) {
            var start = max(session.getStartedAt(), from);
            var end = min(session.getEndedAt(), to);
            for (var day = start.toLocalDate(); day.atStartOfDay().isBefore(end); day = day.plusDays(1)) {
                minutes[day.getDayOfWeek().ordinal()] +=
                        overlapMinutes(day.atStartOfDay(), day.plusDays(1).atStartOfDay(), start, end);
            }
        }
        return minutes;
    }

    /** Equipment of the scope with no minutes of use inside [from, to); all of it when the window is empty. */
    private static int inactiveCount(List<String> equipmentIds, List<UsageSession> sessions,
                                     LocalDateTime from, LocalDateTime to) {
        if (!to.isAfter(from)) return equipmentIds.size();
        var used = new HashSet<String>();
        for (var session : sessions) {
            if (session.minutesWithin(from, to) > 0) used.add(session.getEquipmentId());
        }
        return (int) equipmentIds.stream().filter(id -> !used.contains(id)).count();
    }

    private List<EquipmentUsage> equipmentUsage(List<EquipmentSnapshot> equipment, List<UsageSession> sessions,
                                                LocalDateTime from, LocalDateTime to) {
        Map<String, Double> minutes = new HashMap<>();
        Map<String, Integer> counts = new HashMap<>();
        for (var session : sessions) {
            var within = session.minutesWithin(from, to);
            if (within <= 0) continue;
            minutes.merge(session.getEquipmentId(), within, Double::sum);
            counts.merge(session.getEquipmentId(), 1, Integer::sum);
        }
        var busiest = minutes.values().stream().mapToDouble(Double::doubleValue).max().orElse(0);
        var openMinutes = openMinutesBetween(from, to);

        return equipment.stream().map(e -> {
            var id = e.equipmentId();
            var used = minutes.getOrDefault(id, 0.0);
            return new EquipmentUsage(id, e.zoneId(),
                    round(used), counts.getOrDefault(id, 0),
                    openMinutes > 0 ? round(Math.min(100, used / openMinutes * 100)) : 0,
                    busiest > 0 ? round(used / busiest, 3) : 0);
        }).toList();
    }

    private List<HourlyOccupancy> hourlyOccupancy(List<UsageSession> sessions, int equipmentCount,
                                                  LocalDateTime from, LocalDateTime to) {
        var used = minutesByHourOfDay(sessions, from, to);
        var capacity = new double[24];
        for (var day = from.toLocalDate(); day.atStartOfDay().isBefore(to); day = day.plusDays(1)) {
            for (int hour = openingHour; hour < closingHour; hour++) {
                var slotStart = day.atTime(hour, 0);
                capacity[hour] += overlapMinutes(slotStart, slotStart.plusHours(1), from, to);
            }
        }
        var result = new ArrayList<HourlyOccupancy>();
        for (int hour = openingHour; hour < closingHour; hour++) {
            result.add(new HourlyOccupancy(hour, rate(used[hour], capacity[hour] * equipmentCount)));
        }
        return result;
    }

    /** Occupancy per (weekday, opening hour); slots with no elapsed capacity in the window are skipped. */
    private List<DayHourOccupancy> hourlyOccupancyByDay(List<UsageSession> sessions, int equipmentCount,
                                                        LocalDateTime from, LocalDateTime to) {
        var used = new double[7][24];
        for (var session : sessions) {
            var start = max(session.getStartedAt(), from);
            var end = min(session.getEndedAt(), to);
            for (var slot = start.truncatedTo(ChronoUnit.HOURS); slot.isBefore(end); slot = slot.plusHours(1)) {
                used[slot.getDayOfWeek().ordinal()][slot.getHour()] += overlapMinutes(slot, slot.plusHours(1), start, end);
            }
        }
        var capacity = new double[7][24];
        for (var day = from.toLocalDate(); day.atStartOfDay().isBefore(to); day = day.plusDays(1)) {
            for (int hour = openingHour; hour < closingHour; hour++) {
                var slotStart = day.atTime(hour, 0);
                capacity[day.getDayOfWeek().ordinal()][hour] += overlapMinutes(slotStart, slotStart.plusHours(1), from, to);
            }
        }
        var result = new ArrayList<DayHourOccupancy>();
        for (var day : DayOfWeek.values()) {
            for (int hour = openingHour; hour < closingHour; hour++) {
                var slotCapacity = capacity[day.ordinal()][hour];
                if (slotCapacity <= 0) continue;
                result.add(new DayHourOccupancy(day, hour, rate(used[day.ordinal()][hour], slotCapacity * equipmentCount)));
            }
        }
        return result;
    }

    private double openHoursOccupancy(List<UsageSession> sessions, int equipmentCount,
                                      LocalDateTime from, LocalDateTime to) {
        var used = minutesByHourOfDay(sessions, from, to);
        double openUsed = 0;
        for (int hour = openingHour; hour < closingHour; hour++) openUsed += used[hour];
        return rate(openUsed, openMinutesBetween(from, to) * equipmentCount);
    }

    /** Minutes of use inside [from, to), split by the hour of the day they happened in. */
    private static double[] minutesByHourOfDay(List<UsageSession> sessions, LocalDateTime from, LocalDateTime to) {
        var minutes = new double[24];
        for (var session : sessions) {
            var start = max(session.getStartedAt(), from);
            var end = min(session.getEndedAt(), to);
            for (var slot = start.truncatedTo(ChronoUnit.HOURS); slot.isBefore(end); slot = slot.plusHours(1)) {
                minutes[slot.getHour()] += overlapMinutes(slot, slot.plusHours(1), start, end);
            }
        }
        return minutes;
    }

    private double openMinutesBetween(LocalDateTime from, LocalDateTime to) {
        double minutes = 0;
        for (var day = from.toLocalDate(); day.atStartOfDay().isBefore(to); day = day.plusDays(1)) {
            minutes += overlapMinutes(openingOf(day), closingOf(day), from, to);
        }
        return minutes;
    }

    private LocalDateTime openingOf(LocalDate day) {
        return day.atStartOfDay().plusHours(openingHour);
    }

    private LocalDateTime closingOf(LocalDate day) {
        return day.atStartOfDay().plusHours(closingHour);
    }

    private static double overlapMinutes(LocalDateTime aStart, LocalDateTime aEnd, LocalDateTime bStart, LocalDateTime bEnd) {
        var start = max(aStart, bStart);
        var end = min(aEnd, bEnd);
        return end.isAfter(start) ? elapsed(start, end).toSeconds() / 60.0 : 0;
    }

    /** Elapsed time between two wall-clock readings, measured on the instant line of the system zone. */
    private static Duration elapsed(LocalDateTime from, LocalDateTime to) {
        var zone = ZoneId.systemDefault();
        return Duration.between(from.atZone(zone).toInstant(), to.atZone(zone).toInstant());
    }

    private static double rate(double used, double capacity) {
        return capacity > 0 ? round(Math.min(100, used / capacity * 100)) : 0;
    }

    private static LocalDateTime min(LocalDateTime a, LocalDateTime b) {
        return a.isBefore(b) ? a : b;
    }

    private static LocalDateTime max(LocalDateTime a, LocalDateTime b) {
        return a.isAfter(b) ? a : b;
    }

    private static double round(double value) {
        return round(value, 1);
    }

    private static double round(double value, int decimals) {
        var factor = Math.pow(10, decimals);
        return Math.round(value * factor) / factor;
    }
}
