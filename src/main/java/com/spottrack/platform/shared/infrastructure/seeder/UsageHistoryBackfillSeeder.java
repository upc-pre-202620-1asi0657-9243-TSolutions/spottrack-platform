package com.spottrack.platform.shared.infrastructure.seeder;

import com.spottrack.platform.analytics.domain.model.aggregates.UsageSession;
import com.spottrack.platform.analytics.domain.repositories.ActivityReportRepository;
import com.spottrack.platform.analytics.domain.repositories.UsageSessionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Random;

/**
 * Databases seeded before usage sessions had a time window only hold running totals
 * on ActivityReport, so the heat map has nothing to show. This rebuilds a plausible
 * 30-day session history from each report's total usage, for equipment that has no
 * sessions yet. Sessions are written straight to the repository so the activity
 * reports aren't counted twice.
 *
 * Run with: SPRING_PROFILES_ACTIVE=dev,seed-usage-history (safe to keep enabled).
 */
@Component
@Profile("seed-usage-history")
@Slf4j
public class UsageHistoryBackfillSeeder {

    private static final int HISTORY_DAYS = 30;
    private static final double MEAN_SESSION_MINUTES = 20;

    private final ActivityReportRepository activityReportRepository;
    private final UsageSessionRepository usageSessionRepository;

    public UsageHistoryBackfillSeeder(ActivityReportRepository activityReportRepository,
                                      UsageSessionRepository usageSessionRepository) {
        this.activityReportRepository = activityReportRepository;
        this.usageSessionRepository = usageSessionRepository;
    }

    @EventListener(ApplicationReadyEvent.class)
    @Order(40)
    public void backfill() {
        int equipmentCount = 0;
        int sessionCount = 0;
        for (var report : activityReportRepository.findAll()) {
            var equipmentId = report.getEquipmentId();
            if (equipmentId == null || report.getTotalUsageTime() == null || report.getTotalUsageTime() <= 0) continue;
            if (usageSessionRepository.existsByEquipmentId(equipmentId)) continue;

            var random = new Random(equipmentId.hashCode());
            var sessions = new ArrayList<UsageSession>();
            double remainingSeconds = report.getTotalUsageTime() * 60.0;
            while (remainingSeconds >= 60) {
                long seconds = (long) Math.min(remainingSeconds,
                        Math.max(4 * 60, (MEAN_SESSION_MINUTES + random.nextGaussian() * MEAN_SESSION_MINUTES * 0.4) * 60));
                var startedAt = UsageTimeline.start(UsageTimeline.recentDay(random, HISTORY_DAYS), seconds, random);
                sessions.add(new UsageSession(null, equipmentId, startedAt, startedAt.plusSeconds(seconds)));
                remainingSeconds -= seconds;
            }
            usageSessionRepository.saveAll(sessions);
            equipmentCount++;
            sessionCount += sessions.size();
        }
        log.info("[UsageHistoryBackfillSeeder] Backfilled {} usage sessions for {} equipment.", sessionCount, equipmentCount);
    }
}
