package com.spottrack.platform.monitoring.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "Health of the IoT network: MQTT brokers and how sensor connectivity is decided")
public record MonitoringHealthResource(
        @Schema(description = "UP when every broker is connected, DEGRADED when some are, DOWN when none are")
        String status,
        @Schema(description = "Seconds without a heartbeat after which a sensor is marked offline")
        long heartbeatTimeoutSeconds,
        List<BrokerHealthResource> brokers
) {
    public record BrokerHealthResource(
            String brokerUrl,
            boolean connected,
            LocalDateTime lastConnectedAt,
            LocalDateTime lastDisconnectedAt,
            String lastError
    ) {
    }
}
