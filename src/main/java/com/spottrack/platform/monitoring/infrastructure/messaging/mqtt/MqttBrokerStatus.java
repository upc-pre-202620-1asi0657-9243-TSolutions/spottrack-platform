package com.spottrack.platform.monitoring.infrastructure.messaging.mqtt;

import java.time.LocalDateTime;

public record MqttBrokerStatus(
        String brokerUrl,
        boolean connected,
        LocalDateTime lastConnectedAt,
        LocalDateTime lastDisconnectedAt,
        String lastError
) {
}
