package com.spottrack.platform.monitoring.infrastructure.messaging.mqtt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spottrack.platform.monitoring.application.commandServices.SessionTrackerCommandService;
import com.spottrack.platform.monitoring.domain.model.commands.MotionSensorCaptureCommand;
import com.spottrack.platform.monitoring.domain.model.valueobjects.MotionSensorId;
import com.spottrack.platform.monitoring.domain.repositories.MotionSensorRepository;
import com.spottrack.platform.shared.application.result.Result;
import jakarta.annotation.PostConstruct;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class SensorSignalListener {

    private static final Logger log = LoggerFactory.getLogger(SensorSignalListener.class);
    private static final String TOPIC_FILTER = "sensors/+/signal";
    private static final int QOS = 1;

    private final MqttClient mqttClient;
    private final MotionSensorRepository motionSensorRepository;
    private final SessionTrackerCommandService sessionTrackerCommandService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public SensorSignalListener(MqttClient mqttClient, MotionSensorRepository motionSensorRepository,
                                SessionTrackerCommandService sessionTrackerCommandService) {
        this.mqttClient = mqttClient;
        this.motionSensorRepository = motionSensorRepository;
        this.sessionTrackerCommandService = sessionTrackerCommandService;
    }

    @PostConstruct
    public void subscribe() throws Exception {
        mqttClient.subscribe(TOPIC_FILTER, QOS, (topic, message) -> handleMessage(message));
    }

    private void handleMessage(MqttMessage message) {
        try {
            var signal = objectMapper.readValue(message.getPayload(), SensorSignalMessage.class);
            var sensor = motionSensorRepository.findByMotionSensorId(new MotionSensorId(signal.deviceId()));

            if (sensor.isEmpty()) {
                log.warn("Received MQTT signal for unknown device_id={}", signal.deviceId());
                return;
            }

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
}
