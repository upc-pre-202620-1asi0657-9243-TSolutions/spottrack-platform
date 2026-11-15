package com.spottrack.platform.monitoring.infrastructure.messaging.mqtt.translation;

public record TelemetrySignal(
        String deviceId,
        String signalType,
        long durationMs,
        String timestamp,
        int schemaVersion
) {
    public static final int DEFAULT_SCHEMA_VERSION = 1;

    public TelemetrySignal(String deviceId, String signalType, long durationMs, String timestamp) {
        this(deviceId, signalType, durationMs, timestamp, DEFAULT_SCHEMA_VERSION);
    }
}
