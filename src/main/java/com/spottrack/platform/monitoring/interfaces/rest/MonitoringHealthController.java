package com.spottrack.platform.monitoring.interfaces.rest;

import com.spottrack.platform.monitoring.infrastructure.messaging.mqtt.MqttBrokerPool;
import com.spottrack.platform.monitoring.infrastructure.messaging.mqtt.translation.RejectedSensorSignalRegistry;
import com.spottrack.platform.monitoring.interfaces.rest.resources.MonitoringHealthResource;
import com.spottrack.platform.monitoring.interfaces.rest.resources.MonitoringHealthResource.BrokerHealthResource;
import com.spottrack.platform.monitoring.interfaces.rest.resources.MonitoringHealthResource.RejectedSignalResource;
import com.spottrack.platform.monitoring.interfaces.rest.resources.MonitoringHealthResource.RejectedSignalsResource;
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
    private final RejectedSensorSignalRegistry rejectedSignals;
    private final long heartbeatTimeoutSeconds;

    public MonitoringHealthController(MqttBrokerPool mqttBrokerPool, RejectedSensorSignalRegistry rejectedSignals,
                                      @Value("${monitoring.heartbeat.timeout-seconds:30}") long heartbeatTimeoutSeconds) {
        this.mqttBrokerPool = mqttBrokerPool;
        this.rejectedSignals = rejectedSignals;
        this.heartbeatTimeoutSeconds = heartbeatTimeoutSeconds;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "IoT network health",
            description = "Connection state of every MQTT broker the backend listens to, and the sensor signals discarded because no translator could interpret them.")
    public MonitoringHealthResource getHealth() {
        var brokers = mqttBrokerPool.status().stream()
                .map(b -> new BrokerHealthResource(b.brokerUrl(), b.connected(),
                        b.lastConnectedAt(), b.lastDisconnectedAt(), b.lastError()))
                .toList();
        long connected = brokers.stream().filter(BrokerHealthResource::connected).count();
        String status = connected == brokers.size() && !brokers.isEmpty() ? "UP"
                : connected > 0 ? "DEGRADED"
                : "DOWN";
        return new MonitoringHealthResource(status, heartbeatTimeoutSeconds, brokers, rejectedSignalsResource());
    }

    private RejectedSignalsResource rejectedSignalsResource() {
        var stats = rejectedSignals.stats();
        var last = stats.lastRejected() == null ? null : new RejectedSignalResource(
                stats.lastRejected().deviceId(), stats.lastRejected().payloadProfile(),
                stats.lastRejected().reason(), stats.lastRejected().rejectedAt());
        return new RejectedSignalsResource(stats.total(), stats.byProfile(), last);
    }
}
