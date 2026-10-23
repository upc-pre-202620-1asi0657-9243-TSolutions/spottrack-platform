package com.spottrack.platform.monitoring.infrastructure.messaging.mqtt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spottrack.platform.monitoring.application.commandServices.MotionSensorCommandService;
import com.spottrack.platform.monitoring.application.commandServices.SessionTrackerCommandService;
import com.spottrack.platform.monitoring.domain.model.commands.MotionSensorCaptureCommand;
import com.spottrack.platform.monitoring.domain.model.commands.RecordMotionSensorHeartbeatCommand;
import com.spottrack.platform.monitoring.domain.model.valueobjects.MotionSensorId;
import com.spottrack.platform.monitoring.domain.repositories.MotionSensorRepository;
import com.spottrack.platform.shared.application.result.Result;
import jakarta.annotation.PostConstruct;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Ingests what the IoT devices publish on any broker of the cluster:
 * <ul>
 *   <li>{@code sensors/{deviceId}/signal}: motion detected, feeds the session tracker.</li>
 *   <li>{@code sensors/{deviceId}/heartbeat}: periodic sign of life, keeps the sensor online.</li>
 * </ul>
 * A motion signal also counts as a sign of life.
 */
@Component
public class SensorSignalListener {

    private static final Logger log = LoggerFactory.getLogger(SensorSignalListener.class);
    private static final String SIGNAL_TOPIC_FILTER = "sensors/+/signal";
    private static final String HEARTBEAT_TOPIC_FILTER = "sensors/+/heartbeat";

    private final MqttBrokerPool mqttBrokerPool;
    private final MotionSensorRepository motionSensorRepository;
    private final SessionTrackerCommandService sessionTrackerCommandService;
    private final MotionSensorCommandService motionSensorCommandService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public SensorSignalListener(MqttBrokerPool mqttBrokerPool, MotionSensorRepository motionSensorRepository,
                                SessionTrackerCommandService sessionTrackerCommandService,
                                MotionSensorCommandService motionSensorCommandService) {
        this.mqttBrokerPool = mqttBrokerPool;
        this.motionSensorRepository = motionSensorRepository;
        this.sessionTrackerCommandService = sessionTrackerCommandService;
        this.motionSensorCommandService = motionSensorCommandService;
    }

    @PostConstruct
    public void subscribe() {
        mqttBrokerPool.subscribe(SIGNAL_TOPIC_FILTER, (topic, message) -> handleSignal(topic, message));
        mqttBrokerPool.subscribe(HEARTBEAT_TOPIC_FILTER, (topic, message) -> handleHeartbeat(topic));
    }

    private void handleSignal(String topic, MqttMessage message) {
        try {
            var signal = objectMapper.readValue(message.getPayload(), SensorSignalMessage.class);
            var deviceId = signal.deviceId() != null ? signal.deviceId() : deviceIdFromTopic(topic);
            var sensor = motionSensorRepository.findByMotionSensorId(new MotionSensorId(deviceId));

            if (sensor.isEmpty()) {
                log.warn("Received MQTT signal for unknown device_id={}", deviceId);
                return;
            }

            recordHeartbeat(deviceId);

            var command = new MotionSensorCaptureCommand(sensor.get().getEquipmentId(), true);
            var result = sessionTrackerCommandService.handle(command);

            if (result instanceof Result.Failure<?, ?> failure) {
                log.warn("Failed to record motion sensor reading for equipmentId={}: {}",
                        sensor.get().getEquipmentId().uuid(), failure.error());
            }
        } catch (Exception e) {
            log.error("Error processing MQTT sensor signal", e);
        }
    }

    private void handleHeartbeat(String topic) {
        try {
            recordHeartbeat(deviceIdFromTopic(topic));
        } catch (Exception e) {
            log.error("Error processing MQTT sensor heartbeat on {}", topic, e);
        }
    }

    private void recordHeartbeat(String deviceId) {
        var result = motionSensorCommandService.handle(
                new RecordMotionSensorHeartbeatCommand(new MotionSensorId(deviceId), LocalDateTime.now()));
        if (result instanceof Result.Failure<?, ?> failure) {
            log.debug("Heartbeat ignored for device_id={}: {}", deviceId, failure.error());
        }
    }

    /** {@code sensors/{deviceId}/<kind>} → {@code deviceId} */
    private static String deviceIdFromTopic(String topic) {
        var parts = topic.split("/");
        if (parts.length != 3) throw new IllegalArgumentException("Unexpected MQTT topic: " + topic);
        return parts[1];
    }
}
