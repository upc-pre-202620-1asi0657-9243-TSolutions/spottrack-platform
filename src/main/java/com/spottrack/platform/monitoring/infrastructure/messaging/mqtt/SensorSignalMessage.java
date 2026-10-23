package com.spottrack.platform.monitoring.infrastructure.messaging.mqtt;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The simulator publishes camelCase keys; snake_case is still accepted for older payloads.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record SensorSignalMessage(
        @JsonProperty("deviceId") @JsonAlias("device_id") String deviceId,
        @JsonProperty("signalType") @JsonAlias("signal_type") String signalType,
        @JsonProperty("durationMs") @JsonAlias("duration_ms") long durationMs,
        @JsonProperty("timestamp") String timestamp
) {
}
