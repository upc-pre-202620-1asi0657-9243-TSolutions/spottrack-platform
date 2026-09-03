package com.spottrack.platform.monitoring.infrastructure.messaging.mqtt;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SensorSignalMessage(
        @JsonProperty("device_id") String deviceId,
        @JsonProperty("signal_type") String signalType,
        @JsonProperty("duration_ms") long durationMs,
        @JsonProperty("timestamp") String timestamp
) {
}
