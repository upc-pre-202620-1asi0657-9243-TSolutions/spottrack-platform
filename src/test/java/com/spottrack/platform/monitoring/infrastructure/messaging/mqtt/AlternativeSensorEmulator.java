package com.spottrack.platform.monitoring.infrastructure.messaging.mqtt;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import tools.jackson.databind.json.JsonMapper;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.UUID;

/**
 * IoT emulator of a second manufacturer (profile {@code compact-emulator}): same topics as SpotTrack's own
 * emulator, but compact keys {@code dev}, {@code sig} ("mv" or "hb"), {@code dur} (seconds) and {@code ts} (ISO-8601).
 * The sensor it emulates must be registered with {@code payloadProfile=compact-emulator}.
 * <p>
 * Run it on its own with the settings below ({@code -D} or the matching environment variable):
 * {@code qa01.mqtt.url}, {@code qa01.device.id}, optionally {@code qa01.mqtt.username}, {@code qa01.mqtt.password},
 * {@code qa01.mqtt.ca.file}, {@code qa01.emulator.count} (default 5) and {@code qa01.emulator.interval.ms} (default 1000).
 */
final class AlternativeSensorEmulator implements AutoCloseable {

    private static final JsonMapper MAPPER = JsonMapper.builder().build();
    private static final int QOS = 1;

    private final String deviceId;
    private final MqttClient client;

    AlternativeSensorEmulator(String brokerUrl, String deviceId, MqttConnectionSettings settings) throws MqttException {
        this.deviceId = deviceId;
        this.client = new MqttClient(brokerUrl, "qa01-" + deviceId + "-" + UUID.randomUUID(), new MemoryPersistence());
        var options = new MqttConnectOptions();
        options.setCleanSession(true);
        options.setAutomaticReconnect(false);
        settings.applyTo(options, brokerUrl);
        client.connect(options);
    }

    /** Publishes a motion signal and returns once the broker has acknowledged it (QoS 1). */
    void publishMotion(double durationSeconds) throws MqttException {
        publish("signal", payload(deviceId, "mv", durationSeconds));
    }

    void publishHeartbeat() throws MqttException {
        publish("heartbeat", payload(deviceId, "hb", 0));
    }

    static String payload(String deviceId, String signal, double durationSeconds) {
        var body = new LinkedHashMap<String, Object>();
        body.put("dev", deviceId);
        body.put("sig", signal);
        body.put("dur", durationSeconds);
        body.put("ts", Instant.now().toString());
        return MAPPER.writeValueAsString(body);
    }

    private void publish(String kind, String payload) throws MqttException {
        var message = new MqttMessage(payload.getBytes(StandardCharsets.UTF_8));
        message.setQos(QOS);
        client.publish("sensors/" + deviceId + "/" + kind, message);
    }

    @Override
    public void close() throws MqttException {
        if (client.isConnected()) client.disconnect();
        client.close();
    }

    public static void main(String[] args) throws Exception {
        var count = Qa01Settings.longOrDefault("qa01.emulator.count", 5);
        var intervalMs = Qa01Settings.longOrDefault("qa01.emulator.interval.ms", 1000);
        try (var emulator = new AlternativeSensorEmulator(Qa01Settings.required("qa01.mqtt.url"),
                Qa01Settings.required("qa01.device.id"), Qa01Settings.mqttConnection())) {
            emulator.publishHeartbeat();
            for (long i = 1; i <= count; i++) {
                emulator.publishMotion(1.5);
                System.out.printf("published motion %d/%d%n", i, count);
                if (i < count) Thread.sleep(intervalMs);
            }
        }
    }
}
