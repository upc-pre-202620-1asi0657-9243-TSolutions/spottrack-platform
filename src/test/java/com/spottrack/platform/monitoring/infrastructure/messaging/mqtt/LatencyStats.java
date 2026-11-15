package com.spottrack.platform.monitoring.infrastructure.messaging.mqtt;

import java.util.List;

/** Min, median, p95 (nearest rank) and max of a sample, in the unit of its values. */
record LatencyStats(int count, double min, double median, double p95, double max) {

    static LatencyStats of(List<Double> samples) {
        if (samples.isEmpty()) {
            throw new IllegalArgumentException("No samples");
        }
        var sorted = samples.stream().sorted().toList();
        int n = sorted.size();
        double median = n % 2 == 1 ? sorted.get(n / 2) : (sorted.get(n / 2 - 1) + sorted.get(n / 2)) / 2.0;
        int p95Rank = (int) Math.ceil(0.95 * n);
        return new LatencyStats(n, sorted.get(0), median, sorted.get(p95Rank - 1), sorted.get(n - 1));
    }
}
