package com.spottrack.platform.shared.infrastructure.seeder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Random;

/**
 * Places seeded usage sessions on the clock the way a gym fills up: a morning
 * rush, a quiet midday and the after-work peak, all inside 06:00-22:00.
 */
final class UsageTimeline {

    private static final int OPENING_HOUR = 6;
    private static final int CLOSING_HOUR = 22;
    /** Relative traffic for each hour from 06:00 to 21:00. */
    private static final double[] HOUR_WEIGHTS =
            {0.6, 1.1, 1.5, 1.2, 0.8, 0.6, 0.7, 0.6, 0.5, 0.6, 0.8, 1.2, 1.7, 1.9, 1.5, 0.9};
    private static final double TOTAL_WEIGHT = java.util.Arrays.stream(HOUR_WEIGHTS).sum();

    private UsageTimeline() {
    }

    /** A random day among the last {@code days}, today included. */
    static LocalDate recentDay(Random random, int days) {
        return LocalDate.now().minusDays(random.nextInt(days));
    }

    /** Start time on {@code day} for a session of the given length, ending by closing time. */
    static LocalDateTime start(LocalDate day, long durationSeconds, Random random) {
        double pick = random.nextDouble() * TOTAL_WEIGHT;
        int hour = OPENING_HOUR;
        for (int i = 0; i < HOUR_WEIGHTS.length && pick > HOUR_WEIGHTS[i]; i++) {
            pick -= HOUR_WEIGHTS[i];
            hour++;
        }
        var start = day.atTime(hour, random.nextInt(60), random.nextInt(60));
        var latestStart = day.atTime(CLOSING_HOUR, 0).minusSeconds(durationSeconds);
        return start.isAfter(latestStart) ? latestStart : start;
    }
}
