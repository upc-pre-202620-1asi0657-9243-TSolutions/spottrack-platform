package com.spottrack.platform.monitoring.infrastructure.messaging.mqtt;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LatencyStatsTest {

    @Test
    void singleSampleIsEveryStatistic() {
        var stats = LatencyStats.of(List.of(7.0));

        assertEquals(new LatencyStats(1, 7.0, 7.0, 7.0, 7.0), stats);
    }

    @Test
    void oddSampleMedianIsTheMiddleValueRegardlessOfOrder() {
        var stats = LatencyStats.of(List.of(30.0, 10.0, 20.0));

        assertEquals(10.0, stats.min());
        assertEquals(20.0, stats.median());
        assertEquals(30.0, stats.max());
    }

    @Test
    void evenSampleMedianAveragesTheTwoMiddleValues() {
        assertEquals(25.0, LatencyStats.of(List.of(10.0, 40.0, 20.0, 30.0)).median());
    }

    @Test
    void p95UsesTheNearestRank() {
        var oneToHundred = IntStream.rangeClosed(1, 100).mapToObj(i -> (double) i).toList();
        var oneToFifty = IntStream.rangeClosed(1, 50).mapToObj(i -> (double) i).toList();

        assertEquals(95.0, LatencyStats.of(oneToHundred).p95());
        assertEquals(48.0, LatencyStats.of(oneToFifty).p95());
    }

    @Test
    void p95OfASmallSampleIsItsMaximum() {
        assertEquals(30.0, LatencyStats.of(List.of(10.0, 20.0, 30.0)).p95());
    }

    @Test
    void emptySampleIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> LatencyStats.of(List.of()));
    }
}
