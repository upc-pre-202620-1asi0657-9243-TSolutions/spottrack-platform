package com.spottrack.platform.monitoring.interfaces.rest;

import com.spottrack.platform.monitoring.infrastructure.messaging.mqtt.MqttBrokerPool;
import com.spottrack.platform.monitoring.interfaces.rest.resources.MonitoringHealthResource;
import com.spottrack.platform.monitoring.interfaces.rest.resources.MonitoringHealthResource.BrokerHealthResource;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/api/v1/monitoring/health", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Monitoring Health")
public class MonitoringHealthController {
    private final MqttBrokerPool mqttBrokerPool;
    private final long heartbeatTimeoutSeconds;

    public MonitoringHealthController(MqttBrokerPool mqttBrokerPool,
                                      @Value("${monitoring.heartbeat.timeout-seconds:30}") long heartbeatTimeoutSeconds) {
        this.mqttBrokerPool = mqttBrokerPool;
        this.heartbeatTimeoutSeconds = heartbeatTimeoutSeconds;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "IoT network health", description = "Connection state of every MQTT broker the backend listens to.")
    public MonitoringHealthResource getHealth() {
        var brokers = mqttBrokerPool.status().stream()
                .map(b -> new BrokerHealthResource(b.brokerUrl(), b.connected(),
                        b.lastConnectedAt(), b.lastDisconnectedAt(), b.lastError()))
                .toList();
        long connected = brokers.stream().filter(BrokerHealthResource::connected).count();
        String status = connected == brokers.size() && !brokers.isEmpty() ? "UP"
                : connected > 0 ? "DEGRADED"
                : "DOWN";
        return new MonitoringHealthResource(status, heartbeatTimeoutSeconds, brokers);
    }
}
