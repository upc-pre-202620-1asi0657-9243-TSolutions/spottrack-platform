package com.spottrack.platform.monitoring.domain.model.aggregates;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SessionTrackerActivityTest {

    private static SessionTracker tracker(LocalTime continuousActivity, LocalDateTime lastActivityAt, LocalDateTime createdAt) {
        return new SessionTracker(1L, "tracker-1", "eq-1", null, continuousActivity, LocalTime.MIDNIGHT,
                true, false, lastActivityAt, lastActivityAt, createdAt);
    }

    @Test
    void recordActivitySetsTheContinuousActivityToTheTimeSinceCreation() {
        var tracker = tracker(LocalTime.MIDNIGHT, null, LocalDateTime.now().minusMinutes(10));

        tracker.recordActivity();

        var continuous = Duration.between(LocalTime.MIDNIGHT, tracker.getUsageActivity().continuousActivity());
        assertTrue(continuous.compareTo(Duration.ofMinutes(10)) >= 0 && continuous.compareTo(Duration.ofMinutes(11)) < 0,
                "continuous activity was " + continuous);
        assertEquals(tracker.getLastActivityAt(), tracker.getFirstActivityAt());
    }

    @Test
    void verifyUsageSessionFlagsASessionIdleForThreeMinutes() {
        var tracker = tracker(LocalTime.MIDNIGHT, LocalDateTime.now().minusMinutes(4), LocalDateTime.now().minusMinutes(20));

        assertTrue(tracker.verifyUsageSession());
    }

    @Test
    void verifyUsageSessionKeepsARecentlyActiveSessionAlive() {
        var tracker = tracker(LocalTime.MIDNIGHT, LocalDateTime.now().minusMinutes(1), LocalDateTime.now().minusMinutes(20));

        assertFalse(tracker.verifyUsageSession());
    }

    @Test
    void peekTrueActivitySubtractsTheIdleTimeSinceTheLastActivity() {
        var tracker = tracker(LocalTime.of(0, 30), LocalDateTime.now().minusMinutes(5), LocalDateTime.now().minusMinutes(40));

        var trueActivity = tracker.peekTrueActivity();

        assertTrue(!trueActivity.isAfter(LocalTime.of(0, 25)) && trueActivity.isAfter(LocalTime.of(0, 24)),
                "true activity was " + trueActivity);
    }

    @Test
    void peekTrueActivityIsZeroWithoutAnyRecordedActivity() {
        assertEquals(LocalTime.MIDNIGHT, tracker(LocalTime.of(0, 30), null, LocalDateTime.now()).peekTrueActivity());
    }
}
