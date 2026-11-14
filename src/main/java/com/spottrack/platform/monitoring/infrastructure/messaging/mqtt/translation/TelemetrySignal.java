package com.spottrack.platform.monitoring.infrastructure.messaging.mqtt.translation;

public record TelemetrySignal(
        String deviceId,
        String signalType,
        long durationMs,
        String timestamp
) {
}
