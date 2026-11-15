package com.spottrack.platform.monitoring.infrastructure.messaging.mqtt;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Canonical telemetry contract.
 * The simulator publishes camelCase keys; snake_case is still accepted for older payloads.
 * Implements tolerant reader (unknown fields ignored) and schema versioning (defaults to 1).
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record SensorSignalMessage(
        @JsonProperty("deviceId") @JsonAlias("device_id") String deviceId,
        @JsonProperty("signalType") @JsonAlias("signal_type") String signalType,
        @JsonProperty("durationMs") @JsonAlias("duration_ms") long durationMs,
        @JsonProperty("timestamp") String timestamp,
        @JsonProperty("schemaVersion") @JsonAlias("schema_version") Integer schemaVersion
) {
    public static final int DEFAULT_SCHEMA_VERSION = 1;

    public SensorSignalMessage {
        if (schemaVersion == null || schemaVersion <= 0) {
            schemaVersion = DEFAULT_SCHEMA_VERSION;
        }
    }

    public SensorSignalMessage(String deviceId, String signalType, long durationMs, String timestamp) {
        this(deviceId, signalType, durationMs, timestamp, DEFAULT_SCHEMA_VERSION);
    }
}
