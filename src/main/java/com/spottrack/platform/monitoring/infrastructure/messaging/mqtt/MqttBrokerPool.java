package com.spottrack.platform.monitoring.infrastructure.messaging.mqtt;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Subscribes to every MQTT broker of the cluster.
 * <p>
 * Devices publish through the TCP load balancer, which pins each connection to
 * one of the (non-bridged) brokers. Listening to all of them is the only way the
 * backend sees every device, and it also lets us report each broker's health.
 * Configure the brokers with {@code mqtt.broker-urls} (comma separated).
 */
public class MqttBrokerPool {

    private static final Logger log = LoggerFactory.getLogger(MqttBrokerPool.class);
    private static final long RECONNECT_INTERVAL_SECONDS = 10;

    private final List<String> brokerUrls;
    private final Map<String, IMqttMessageListener> subscriptions = new ConcurrentHashMap<>();
    private final List<MqttBrokerConnection> connections = new ArrayList<>();
    private final ScheduledExecutorService reconnector = Executors.newSingleThreadScheduledExecutor(r -> {
        var thread = new Thread(r, "mqtt-reconnector");
        thread.setDaemon(true);
        return thread;
    });

    public MqttBrokerPool(List<String> brokerUrls) {
        this.brokerUrls = brokerUrls.stream().map(String::trim).filter(url -> !url.isEmpty()).distinct().toList();
    }

    @PostConstruct
    void start() throws MqttException {
        for (var url : brokerUrls) {
            connections.add(new MqttBrokerConnection(url, subscriptions));
        }
        log.info("MQTT subscribing to {} broker(s): {}", connections.size(), brokerUrls);
        connections.forEach(MqttBrokerConnection::ensureConnected);
        reconnector.scheduleWithFixedDelay(
                () -> connections.forEach(MqttBrokerConnection::ensureConnected),
                RECONNECT_INTERVAL_SECONDS, RECONNECT_INTERVAL_SECONDS, TimeUnit.SECONDS);
    }

    /** Subscribes on every broker, now and after each (re)connection. */
    public void subscribe(String topicFilter, IMqttMessageListener listener) {
        subscriptions.put(topicFilter, listener);
        connections.forEach(connection -> connection.subscribe(topicFilter, listener));
    }

    public List<MqttBrokerStatus> status() {
        return connections.stream().map(MqttBrokerConnection::status).toList();
    }

    @PreDestroy
    void stop() {
        reconnector.shutdownNow();
        connections.forEach(MqttBrokerConnection::disconnect);
    }
}
