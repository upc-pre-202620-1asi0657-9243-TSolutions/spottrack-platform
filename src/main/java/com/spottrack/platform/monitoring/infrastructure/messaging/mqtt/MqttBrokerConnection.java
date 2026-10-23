package com.spottrack.platform.monitoring.infrastructure.messaging.mqtt;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * One subscriber connection to a single Mosquitto broker.
 * <p>
 * The brokers behind the load balancer are independent (no bridging), so a
 * device's messages only reach the broker the load balancer routed it to.
 * The backend therefore keeps one of these per broker and listens to all of them.
 */
class MqttBrokerConnection implements MqttCallbackExtended {

    private static final Logger log = LoggerFactory.getLogger(MqttBrokerConnection.class);
    private static final int QOS = 1;

    private final String brokerUrl;
    private final Map<String, IMqttMessageListener> subscriptions;
    private final MqttClient client;
    private final MqttConnectOptions options;

    private volatile LocalDateTime lastConnectedAt;
    private volatile LocalDateTime lastDisconnectedAt;
    private volatile String lastError;

    MqttBrokerConnection(String brokerUrl, Map<String, IMqttMessageListener> subscriptions) throws MqttException {
        this.brokerUrl = brokerUrl;
        this.subscriptions = subscriptions;
        this.client = new MqttClient(brokerUrl, "spottrack-backend-" + UUID.randomUUID(), new MemoryPersistence());
        this.client.setCallback(this);

        this.options = new MqttConnectOptions();
        this.options.setCleanSession(true);
        this.options.setConnectionTimeout(5);
        this.options.setKeepAliveInterval(20);
        // Reconnection is driven by MqttBrokerPool, which also covers brokers
        // that were already down when the backend started.
        this.options.setAutomaticReconnect(false);
    }

    /** Connects if not connected yet. Never throws: a broker being down must not stop the backend. */
    synchronized void ensureConnected() {
        if (client.isConnected()) return;
        try {
            client.connect(options);
        } catch (MqttException e) {
            lastError = e.getMessage();
            log.warn("MQTT broker {} unreachable: {}", brokerUrl, e.getMessage());
        }
    }

    synchronized void subscribe(String topicFilter, IMqttMessageListener listener) {
        if (!client.isConnected()) return; // applied on the next connectComplete
        try {
            client.subscribe(topicFilter, QOS, listener);
        } catch (MqttException e) {
            log.warn("MQTT subscribe to {} on {} failed: {}", topicFilter, brokerUrl, e.getMessage());
        }
    }

    synchronized void disconnect() {
        try {
            if (client.isConnected()) client.disconnect();
            client.close();
        } catch (MqttException e) {
            log.debug("MQTT disconnect from {} failed: {}", brokerUrl, e.getMessage());
        }
    }

    @Override
    public void connectComplete(boolean reconnect, String serverURI) {
        lastConnectedAt = LocalDateTime.now();
        lastError = null;
        log.info("MQTT broker {} connected", brokerUrl);
        // cleanSession=true drops subscriptions on every new session, so restore them.
        subscriptions.forEach(this::subscribe);
    }

    @Override
    public void connectionLost(Throwable cause) {
        lastDisconnectedAt = LocalDateTime.now();
        lastError = cause != null ? cause.getMessage() : "connection lost";
        log.warn("MQTT broker {} connection lost: {}", brokerUrl, lastError);
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) {
        // handled by the per-subscription listeners
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        // backend only subscribes
    }

    MqttBrokerStatus status() {
        return new MqttBrokerStatus(brokerUrl, client.isConnected(), lastConnectedAt, lastDisconnectedAt, lastError);
    }
}
