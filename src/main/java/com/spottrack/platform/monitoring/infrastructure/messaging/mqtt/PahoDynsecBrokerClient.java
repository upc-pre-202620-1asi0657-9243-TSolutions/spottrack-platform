package com.spottrack.platform.monitoring.infrastructure.messaging.mqtt;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Talks to the dynamic-security plugin of one broker over a short-lived TLS connection
 * authenticated with the dynsec admin account (not the backend's subscriber account).
 * <p>
 * Each call opens its own connection, so nothing is shared between brokers or requests.
 * The request and the answers are matched by a random id carried in {@code correlationData}
 * because every client subscribed to the response topic receives every answer.
 */
class PahoDynsecBrokerClient implements DynsecBrokerClient {
    static final String CONTROL_TOPIC = "$CONTROL/dynamic-security/v1";
    static final String RESPONSE_TOPIC = CONTROL_TOPIC + "/response";
    private static final Logger log = LoggerFactory.getLogger(PahoDynsecBrokerClient.class);
    private static final int QOS = 1;
    private static final JsonMapper MAPPER = JsonMapper.builder().build();

    private final MqttConnectionSettings adminSettings;
    private final Duration connectTimeout;
    private final Duration responseTimeout;

    PahoDynsecBrokerClient(MqttConnectionSettings adminSettings, Duration connectTimeout, Duration responseTimeout) {
        this.adminSettings = adminSettings;
        this.connectTimeout = connectTimeout;
        this.responseTimeout = responseTimeout;
    }

    @Override
    public List<DynsecResult> execute(String brokerUrl, List<DynsecCommand> commands) throws DynsecTransportException {
        var requestId = UUID.randomUUID().toString();
        var request = buildRequest(requestId, commands).getBytes(StandardCharsets.UTF_8);
        var answer = new CompletableFuture<List<DynsecResult>>();
        MqttClient client = null;
        try {
            client = new MqttClient(brokerUrl, "spottrack-dynsec-" + UUID.randomUUID(), new MemoryPersistence());
            client.setTimeToWait(responseTimeout.toMillis());
            client.connect(connectOptions(brokerUrl));
            client.subscribe(RESPONSE_TOPIC, QOS, (topic, message) ->
                    parseResponse(requestId, new String(message.getPayload(), StandardCharsets.UTF_8))
                            .ifPresent(answer::complete));
            client.publish(CONTROL_TOPIC, request, QOS, false);
            return answer.get(responseTimeout.toMillis(), TimeUnit.MILLISECONDS);
        } catch (MqttException e) {
            throw new DynsecTransportException("MQTT error " + e.getReasonCode() + " (" + e.getMessage() + ")", e);
        } catch (TimeoutException e) {
            throw new DynsecTransportException("no answer from the broker in " + responseTimeout.toMillis() + " ms", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new DynsecTransportException("interrupted", e);
        } catch (ExecutionException e) {
            throw new DynsecTransportException("unreadable answer from the broker", e);
        } finally {
            closeQuietly(client, brokerUrl);
        }
    }

    private MqttConnectOptions connectOptions(String brokerUrl) {
        var options = new MqttConnectOptions();
        options.setCleanSession(true);
        options.setAutomaticReconnect(false);
        options.setConnectionTimeout((int) Math.max(1, connectTimeout.toSeconds()));
        adminSettings.applyTo(options, brokerUrl);
        return options;
    }

    private void closeQuietly(MqttClient client, String brokerUrl) {
        if (client == null) return;
        try {
            if (client.isConnected()) client.disconnect(1000);
        } catch (MqttException e) {
            try {
                client.disconnectForcibly(0, 500);
            } catch (MqttException ignored) {
                log.debug("Could not close the dynsec connection to {}", brokerUrl);
            }
        }
        try {
            client.close();
        } catch (MqttException e) {
            log.debug("Could not release the dynsec client of {}", brokerUrl);
        }
    }

    static String buildRequest(String requestId, List<DynsecCommand> commands) {
        var entries = new ArrayList<LinkedHashMap<String, Object>>();
        for (int i = 0; i < commands.size(); i++) {
            var entry = new LinkedHashMap<String, Object>();
            entry.put("command", commands.get(i).name());
            entry.putAll(commands.get(i).arguments());
            entry.put("correlationData", requestId + ":" + i);
            entries.add(entry);
        }
        return MAPPER.writeValueAsString(java.util.Map.of("commands", entries));
    }

    /** @return the results when the message answers {@code requestId}; empty for anyone else's answer or garbage */
    static Optional<List<DynsecResult>> parseResponse(String requestId, String payload) {
        JsonNode responses;
        try {
            responses = MAPPER.readTree(payload).path("responses");
        } catch (RuntimeException e) {
            return Optional.empty();
        }
        if (!responses.isArray() || responses.isEmpty()) return Optional.empty();

        var results = new ArrayList<DynsecResult>();
        for (var response : responses) {
            if (!response.path("correlationData").asString("").startsWith(requestId + ":")) return Optional.empty();
            var error = response.hasNonNull("error") ? response.path("error").asString() : null;
            results.add(new DynsecResult(response.path("command").asString(""), error));
        }
        return Optional.of(results);
    }
}
