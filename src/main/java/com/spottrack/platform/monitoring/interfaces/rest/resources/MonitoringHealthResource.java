package com.spottrack.platform.monitoring.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Schema(description = "Health of the IoT network: MQTT brokers and how sensor connectivity is decided")
public record MonitoringHealthResource(
        @Schema(description = "UP when every broker is connected, DEGRADED when some are, DOWN when none are")
        String status,
        @Schema(description = "Seconds without a heartbeat after which a sensor is marked offline")
        long heartbeatTimeoutSeconds,
        List<BrokerHealthResource> brokers,
        @Schema(description = "Sensor signals discarded because no translator could interpret them, since the backend started")
        RejectedSignalsResource rejectedSignals
) {
    public record BrokerHealthResource(
            String brokerUrl,
            boolean connected,
            LocalDateTime lastConnectedAt,
            LocalDateTime lastDisconnectedAt,
            String lastError
    ) {
    }

    public record RejectedSignalsResource(
            @Schema(description = "Discarded signals, all profiles")
            long total,
            @Schema(description = "Discarded signals per payload profile; a rising count points to a misconfigured manufacturer")
            Map<String, Long> byProfile,
            @Schema(description = "Most recent discarded signal, null when there is none")
            RejectedSignalResource lastRejected
    ) {
    }

    public record RejectedSignalResource(
            String deviceId,
            String payloadProfile,
            String reason,
            LocalDateTime rejectedAt
    ) {
    }
}
