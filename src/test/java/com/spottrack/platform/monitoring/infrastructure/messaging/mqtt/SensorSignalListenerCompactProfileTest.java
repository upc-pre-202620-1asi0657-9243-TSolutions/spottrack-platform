package com.spottrack.platform.monitoring.infrastructure.messaging.mqtt;

import com.spottrack.platform.monitoring.application.commandServices.MotionSensorCommandService;
import com.spottrack.platform.monitoring.application.commandServices.SensorPairingCommandService;
import com.spottrack.platform.monitoring.application.commandServices.SessionTrackerCommandService;
import com.spottrack.platform.monitoring.domain.model.aggregates.MotionSensor;
import com.spottrack.platform.monitoring.domain.model.commands.MotionSensorCaptureCommand;
import com.spottrack.platform.monitoring.domain.model.valueobjects.EquipmentId;
import com.spottrack.platform.monitoring.domain.model.valueobjects.MotionSensorId;
import com.spottrack.platform.monitoring.domain.repositories.MotionSensorRepository;
import com.spottrack.platform.monitoring.infrastructure.messaging.mqtt.translation.CompactSensorPayloadTranslator;
import com.spottrack.platform.monitoring.infrastructure.messaging.mqtt.translation.DefaultSensorPayloadTranslator;
import com.spottrack.platform.monitoring.infrastructure.messaging.mqtt.translation.RejectedSensorSignalRegistry;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** The listener with the real translators: a compact-emulator sensor is read with the compact format only. */
class SensorSignalListenerCompactProfileTest {

    private static final String TOPIC = "sensors/dev-1/signal";
    private static final String COMPACT_PAYLOAD = "{\"dev\":\"dev-1\",\"sig\":\"mv\",\"dur\":0.8,\"ts\":\"2026-11-15T12:00:00Z\"}";
    private static final String DEFAULT_PAYLOAD = "{\"deviceId\":\"dev-1\",\"signalType\":\"MOTION\",\"durationMs\":800}";

    private MotionSensorRepository motionSensorRepository;
    private SessionTrackerCommandService sessionTrackerCommandService;
    private RejectedSensorSignalRegistry rejectedSignals;
    private IMqttMessageListener signalHandler;

    @BeforeEach
    void setUp() {
        var mqttBrokerPool = mock(MqttBrokerPool.class);
        motionSensorRepository = mock(MotionSensorRepository.class);
        sessionTrackerCommandService = mock(SessionTrackerCommandService.class);
        var mapper = JsonMapper.builder().build();
        var selector = new SensorPayloadTranslatorSelector(
                List.of(new DefaultSensorPayloadTranslator(mapper), new CompactSensorPayloadTranslator(mapper)));
        rejectedSignals = new RejectedSensorSignalRegistry();

        new SensorSignalListener(mqttBrokerPool, motionSensorRepository, sessionTrackerCommandService,
                mock(MotionSensorCommandService.class), selector, rejectedSignals,
                mock(SensorPairingCommandService.class)).subscribe();

        var captor = ArgumentCaptor.forClass(IMqttMessageListener.class);
        verify(mqttBrokerPool).subscribe(eq("sensors/+/signal"), captor.capture());
        signalHandler = captor.getValue();

        var now = LocalDateTime.now();
        var sensor = new MotionSensor(1L, new MotionSensorId("dev-1"), new EquipmentId("eq-7"), now, false, now, null,
                "compact-emulator");
        when(motionSensorRepository.findByMotionSensorId(new MotionSensorId("dev-1"))).thenReturn(Optional.of(sensor));
    }

    @Test
    void compactPayloadFromACompactEmulatorSensorReachesTheDomainCommand() throws Exception {
        send(COMPACT_PAYLOAD);

        verify(sessionTrackerCommandService).handle(new MotionSensorCaptureCommand(new EquipmentId("eq-7"), true));
        assertEquals(0, rejectedSignals.stats().total());
    }

    @Test
    void canonicalPayloadFromACompactEmulatorSensorIsRejectedUnderThatProfile() throws Exception {
        send(DEFAULT_PAYLOAD);

        verify(sessionTrackerCommandService, never()).handle(any(MotionSensorCaptureCommand.class));
        assertEquals(1L, rejectedSignals.stats().byProfile().get("compact-emulator"));
    }

    @Test
    void compactPayloadWithoutDevIsRejectedUnderThatProfile() throws Exception {
        send("{\"sig\":\"mv\",\"dur\":1}");

        verify(sessionTrackerCommandService, never()).handle(any(MotionSensorCaptureCommand.class));
        assertEquals(1L, rejectedSignals.stats().byProfile().get("compact-emulator"));
        assertEquals("dev-1", rejectedSignals.stats().lastRejected().deviceId());
    }

    private void send(String payload) throws Exception {
        signalHandler.messageArrived(TOPIC, new MqttMessage(payload.getBytes(StandardCharsets.UTF_8)));
    }
}
