package com.spottrack.platform.monitoring.infrastructure.messaging.mqtt;

import com.spottrack.platform.monitoring.application.commandServices.MotionSensorCommandService;
import com.spottrack.platform.monitoring.application.commandServices.SessionTrackerCommandService;
import com.spottrack.platform.monitoring.domain.model.aggregates.MotionSensor;
import com.spottrack.platform.monitoring.domain.model.commands.MotionSensorCaptureCommand;
import com.spottrack.platform.monitoring.domain.model.valueobjects.EquipmentId;
import com.spottrack.platform.monitoring.domain.model.valueobjects.MotionSensorId;
import com.spottrack.platform.monitoring.domain.repositories.MotionSensorRepository;
import com.spottrack.platform.monitoring.infrastructure.messaging.mqtt.translation.DefaultSensorPayloadTranslator;
import com.spottrack.platform.monitoring.infrastructure.messaging.mqtt.translation.SensorPayloadTranslatorSelector;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import tools.jackson.databind.json.JsonMapper;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SensorSignalListenerTest {

    private static final String TOPIC = "sensors/dev-1/signal";
    private static final String VALID_PAYLOAD = "{\"deviceId\":\"dev-1\",\"signalType\":\"MOTION\",\"durationMs\":800}";

    private MqttBrokerPool mqttBrokerPool;
    private MotionSensorRepository motionSensorRepository;
    private SessionTrackerCommandService sessionTrackerCommandService;
    private MotionSensorCommandService motionSensorCommandService;
    private IMqttMessageListener signalHandler;

    @BeforeEach
    void setUp() {
        mqttBrokerPool = mock(MqttBrokerPool.class);
        motionSensorRepository = mock(MotionSensorRepository.class);
        sessionTrackerCommandService = mock(SessionTrackerCommandService.class);
        motionSensorCommandService = mock(MotionSensorCommandService.class);
        var selector = new SensorPayloadTranslatorSelector(
                List.of(new DefaultSensorPayloadTranslator(JsonMapper.builder().build())));

        new SensorSignalListener(mqttBrokerPool, motionSensorRepository, sessionTrackerCommandService,
                motionSensorCommandService, selector).subscribe();

        var captor = ArgumentCaptor.forClass(IMqttMessageListener.class);
        verify(mqttBrokerPool).subscribe(eq("sensors/+/signal"), captor.capture());
        signalHandler = captor.getValue();
    }

    @Test
    void unknownSensorDoesNotExecuteTheCommand() throws Exception {
        when(motionSensorRepository.findByMotionSensorId(new MotionSensorId("dev-1"))).thenReturn(Optional.empty());

        send(VALID_PAYLOAD);

        verify(sessionTrackerCommandService, never()).handle(any(MotionSensorCaptureCommand.class));
    }

    @Test
    void sensorWithoutProfileExecutesTheCaptureCommandForItsEquipment() throws Exception {
        givenRegisteredSensor("eq-7");

        send(VALID_PAYLOAD);

        verify(sessionTrackerCommandService).handle(new MotionSensorCaptureCommand(new EquipmentId("eq-7"), true));
    }

    @Test
    void invalidPayloadDoesNotExecuteTheCommand() throws Exception {
        givenRegisteredSensor("eq-7");

        send("not a json");

        verify(sessionTrackerCommandService, never()).handle(any(MotionSensorCaptureCommand.class));
    }

    private void givenRegisteredSensor(String equipmentId) {
        var now = LocalDateTime.now();
        var sensor = new MotionSensor(1L, new MotionSensorId("dev-1"), new EquipmentId(equipmentId), now, false, now, null);
        when(motionSensorRepository.findByMotionSensorId(new MotionSensorId("dev-1"))).thenReturn(Optional.of(sensor));
    }

    private void send(String payload) throws Exception {
        signalHandler.messageArrived(TOPIC, new MqttMessage(payload.getBytes(StandardCharsets.UTF_8)));
    }
}
