package com.spottrack.platform.monitoring.infrastructure.messaging.mqtt.translation;

import com.spottrack.platform.shared.application.result.ApplicationError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.LongAdder;

/**
 * Logs and counts the MQTT signals that no translator could interpret (Iteration 6, QA-06).
 * <p>
 * A rising count for one profile points to a manufacturer whose sensors are misconfigured
 * or publish a format the profile does not cover. Counts live in memory and restart with
 * the backend; they are exposed through the monitoring health endpoint.
 */
@Component
public class RejectedSensorSignalRegistry {

    private static final Logger log = LoggerFactory.getLogger(RejectedSensorSignalRegistry.class);

    private final Map<String, LongAdder> rejectionsByProfile = new ConcurrentHashMap<>();
    private volatile RejectedSensorSignal lastRejected;

    /** Records a discarded signal. The profile is the sensor's one, null meaning the default format. */
    public void record(String deviceId, String payloadProfile, ApplicationError error) {
        var profile = SensorPayloadTranslatorSelector.resolveProfile(payloadProfile);
        log.warn("Rejected MQTT signal device_id={} profile={}: {}", deviceId, profile, error);
        rejectionsByProfile.computeIfAbsent(profile, p -> new LongAdder()).increment();
        lastRejected = new RejectedSensorSignal(deviceId, profile, reason(error), LocalDateTime.now());
    }

    private static String reason(ApplicationError error) {
        return error.details() == null || error.details().isBlank()
                ? error.message()
                : error.message() + ": " + error.details();
    }

    public RejectedSensorSignalStats stats() {
        var byProfile = new TreeMap<String, Long>();
        rejectionsByProfile.forEach((profile, count) -> byProfile.put(profile, count.sum()));
        long total = byProfile.values().stream().mapToLong(Long::longValue).sum();
        return new RejectedSensorSignalStats(total, byProfile, lastRejected);
    }

    public record RejectedSensorSignal(String deviceId, String payloadProfile, String reason,
                                       LocalDateTime rejectedAt) {
    }

    public record RejectedSensorSignalStats(long total, Map<String, Long> byProfile,
                                            RejectedSensorSignal lastRejected) {
    }
}
