package com.spottrack.platform.monitoring.infrastructure.messaging.mqtt;

import com.spottrack.platform.monitoring.application.commandServices.MotionSensorCommandService;
import com.spottrack.platform.monitoring.application.commandServices.SensorPairingCommandService;
import com.spottrack.platform.monitoring.application.commandServices.SessionTrackerCommandService;
import com.spottrack.platform.monitoring.domain.model.aggregates.MotionSensor;
import com.spottrack.platform.monitoring.domain.model.commands.MarkSensorPairingUsedCommand;
import com.spottrack.platform.monitoring.domain.model.commands.MotionSensorCaptureCommand;
import com.spottrack.platform.monitoring.domain.model.commands.RecordMotionSensorHeartbeatCommand;
import com.spottrack.platform.monitoring.domain.model.valueobjects.EquipmentId;
import com.spottrack.platform.monitoring.domain.model.valueobjects.MotionSensorId;
import com.spottrack.platform.monitoring.domain.repositories.MotionSensorRepository;
import com.spottrack.platform.monitoring.infrastructure.messaging.mqtt.translation.DefaultSensorPayloadTranslator;
import com.spottrack.platform.monitoring.infrastructure.messaging.mqtt.translation.RejectedSensorSignalRegistry;
import com.spottrack.platform.monitoring.infrastructure.messaging.mqtt.translation.SensorPayloadTranslator;
import com.spottrack.platform.monitoring.infrastructure.messaging.mqtt.translation.SensorPayloadTranslatorSelector;
import com.spottrack.platform.monitoring.infrastructure.messaging.mqtt.translation.TelemetrySignal;
import com.spottrack.platform.shared.application.result.ApplicationError;
import com.spottrack.platform.shared.application.result.Result;
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

class SensorSignalListenerTest {

    private static final String TOPIC = "sensors/dev-1/signal";
    private static final String ALT_PAYLOAD = "{\"id\":\"dev-1\",\"evt\":\"mv\",\"ms\":800}";
    private static final String VALID_PAYLOAD = "{\"deviceId\":\"dev-1\",\"signalType\":\"MOTION\",\"durationMs\":800}";

    private MqttBrokerPool mqttBrokerPool;
    private MotionSensorRepository motionSensorRepository;
    private SessionTrackerCommandService sessionTrackerCommandService;
    private MotionSensorCommandService motionSensorCommandService;
    private SensorPayloadTranslator altTranslator;
    private RejectedSensorSignalRegistry rejectedSignals;
    private SensorPairingCommandService sensorPairingCommandService;
    private IMqttMessageListener signalHandler;
    private IMqttMessageListener heartbeatHandler;

    @BeforeEach
    void setUp() {
        mqttBrokerPool = mock(MqttBrokerPool.class);
        motionSensorRepository = mock(MotionSensorRepository.class);
        sessionTrackerCommandService = mock(SessionTrackerCommandService.class);
        motionSensorCommandService = mock(MotionSensorCommandService.class);
        sensorPairingCommandService = mock(SensorPairingCommandService.class);
        altTranslator = mock(SensorPayloadTranslator.class);
        when(altTranslator.profile()).thenReturn("alt");
        when(altTranslator.translate(any())).thenReturn(Result.success(new TelemetrySignal("dev-1", "MOTION", 800, null)));
        var selector = new SensorPayloadTranslatorSelector(
                List.of(new DefaultSensorPayloadTranslator(JsonMapper.builder().build()), altTranslator));

        rejectedSignals = new RejectedSensorSignalRegistry();

        new SensorSignalListener(mqttBrokerPool, motionSensorRepository, sessionTrackerCommandService,
                motionSensorCommandService, selector, rejectedSignals, sensorPairingCommandService).subscribe();

        var captor = ArgumentCaptor.forClass(IMqttMessageListener.class);
        verify(mqttBrokerPool).subscribe(eq("sensors/+/signal"), captor.capture());
        signalHandler = captor.getValue();

        var heartbeatCaptor = ArgumentCaptor.forClass(IMqttMessageListener.class);
        verify(mqttBrokerPool).subscribe(eq("sensors/+/heartbeat"), heartbeatCaptor.capture());
        heartbeatHandler = heartbeatCaptor.getValue();
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
    void sensorWithoutProfileUsesTheDefaultTranslatorNotTheAlternativeOne() throws Exception {
        givenRegisteredSensor("eq-7");

        send(VALID_PAYLOAD);

        verify(altTranslator, never()).translate(any());
        verify(sessionTrackerCommandService).handle(new MotionSensorCaptureCommand(new EquipmentId("eq-7"), true));
    }

    @Test
    void sensorWithAltProfileUsesTheAlternativeTranslatorInsteadOfTheDefault() throws Exception {
        givenRegisteredSensor("eq-7", "alt");

        send(ALT_PAYLOAD);

        verify(altTranslator).translate(ALT_PAYLOAD);
        verify(sessionTrackerCommandService).handle(new MotionSensorCaptureCommand(new EquipmentId("eq-7"), true));
        assertEquals(0, rejectedSignals.stats().total());
    }

    @Test
    void sensorWithAltProfileIsNotReadWithTheDefaultFormat() throws Exception {
        givenRegisteredSensor("eq-7", "alt");

        send(VALID_PAYLOAD);

        verify(altTranslator).translate(VALID_PAYLOAD);
        assertEquals(0, rejectedSignals.stats().total());
    }

    @Test
    void sensorWithAProfileNoTranslatorSupportsIsRejectedUnderThatProfile() throws Exception {
        givenRegisteredSensor("eq-7", "retired");

        send(VALID_PAYLOAD);

        verify(sessionTrackerCommandService, never()).handle(any(MotionSensorCaptureCommand.class));
        assertEquals(1L, rejectedSignals.stats().byProfile().get("retired"));
    }

    @Test
    void invalidPayloadDoesNotExecuteTheCommand() throws Exception {
        givenRegisteredSensor("eq-7");

        send("not a json");

        verify(sessionTrackerCommandService, never()).handle(any(MotionSensorCaptureCommand.class));
    }

    @Test
    void invalidPayloadIsRecordedWithItsDeviceAndProfile() throws Exception {
        givenRegisteredSensor("eq-7");

        send("not a json");

        var stats = rejectedSignals.stats();
        assertEquals(1, stats.total());
        assertEquals(1L, stats.byProfile().get("default"));
        assertEquals("dev-1", stats.lastRejected().deviceId());
        assertEquals("default", stats.lastRejected().payloadProfile());
    }

    @Test
    void validPayloadIsNotCountedAsRejected() throws Exception {
        givenRegisteredSensor("eq-7");

        send(VALID_PAYLOAD);

        assertEquals(0, rejectedSignals.stats().total());
    }

    @Test
    void payloadWithoutSchemaVersionDefaultsToVersion1AndExecutesDomainCommand() throws Exception {
        givenRegisteredSensor("eq-7");

        send("{\"deviceId\":\"dev-1\",\"signalType\":\"MOTION\",\"durationMs\":950}");

        verify(sessionTrackerCommandService).handle(new MotionSensorCaptureCommand(new EquipmentId("eq-7"), true));
        assertEquals(0, rejectedSignals.stats().total());
    }

    @Test
    void payloadWithExtraUnknownFieldsExecutesDomainCommand() throws Exception {
        givenRegisteredSensor("eq-7");

        send("{\"deviceId\":\"dev-1\",\"signalType\":\"MOTION\",\"durationMs\":950,\"battery\":98,\"unknownAttr\":\"val\"}");

        verify(sessionTrackerCommandService).handle(new MotionSensorCaptureCommand(new EquipmentId("eq-7"), true));
        assertEquals(0, rejectedSignals.stats().total());
    }

    @Test
    void signalOfUnknownSensorIsNotCountedAsRejected() throws Exception {
        when(motionSensorRepository.findByMotionSensorId(new MotionSensorId("dev-1"))).thenReturn(Optional.empty());

        send("not a json");

        assertEquals(0, rejectedSignals.stats().total());
    }

    @Test
    void heartbeatMarksThePairingOfTheDeviceAsUsed() throws Exception {
        when(motionSensorCommandService.handle(any(RecordMotionSensorHeartbeatCommand.class)))
                .thenReturn(Result.success(null));

        heartbeatHandler.messageArrived("sensors/dev-1/heartbeat", new MqttMessage(new byte[0]));

        verify(sensorPairingCommandService).handle(new MarkSensorPairingUsedCommand(new MotionSensorId("dev-1")));
    }

    @Test
    void ignoredHeartbeatDoesNotTouchThePairing() throws Exception {
        when(motionSensorCommandService.handle(any(RecordMotionSensorHeartbeatCommand.class)))
                .thenReturn(Result.failure(ApplicationError.notFound("MotionSensor", "dev-1")));

        heartbeatHandler.messageArrived("sensors/dev-1/heartbeat", new MqttMessage(new byte[0]));

        verify(sensorPairingCommandService, never()).handle(any(MarkSensorPairingUsedCommand.class));
    }

    @Test
    void motionSignalAlsoMarksThePairingAsUsed() throws Exception {
        givenRegisteredSensor("eq-7");
        when(motionSensorCommandService.handle(any(RecordMotionSensorHeartbeatCommand.class)))
                .thenReturn(Result.success(null));

        send(VALID_PAYLOAD);

        verify(sensorPairingCommandService).handle(new MarkSensorPairingUsedCommand(new MotionSensorId("dev-1")));
    }

    private void givenRegisteredSensor(String equipmentId) {
        givenRegisteredSensor(equipmentId, null);
    }

    private void givenRegisteredSensor(String equipmentId, String payloadProfile) {
        var now = LocalDateTime.now();
        var sensor = new MotionSensor(1L, new MotionSensorId("dev-1"), new EquipmentId(equipmentId), now, false, now, null,
                payloadProfile);
        when(motionSensorRepository.findByMotionSensorId(new MotionSensorId("dev-1"))).thenReturn(Optional.of(sensor));
    }

    private void send(String payload) throws Exception {
        signalHandler.messageArrived(TOPIC, new MqttMessage(payload.getBytes(StandardCharsets.UTF_8)));
    }
}
