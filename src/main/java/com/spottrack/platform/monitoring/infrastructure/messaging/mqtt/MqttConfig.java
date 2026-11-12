package com.spottrack.platform.monitoring.infrastructure.messaging.mqtt;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class MqttConfig {

    /**
     * Credentials and TLS trust for the hardened brokers (ssl://...:8883).
     * All empty means anonymous plain TCP, e.g. a local Mosquitto.
     */
    @Bean
    public MqttConnectionSettings mqttConnectionSettings(
            @Value("${mqtt.username:}") String username,
            @Value("${mqtt.password:}") String password,
            @Value("${mqtt.ca-cert:}") String caCertificate) {
        return new MqttConnectionSettings(username, password, caCertificate);
    }

    /**
     * One subscriber connection per broker of the cluster. Falls back to the
     * single {@code mqtt.broker-url} when {@code mqtt.broker-urls} is not set.
     */
    @Bean
    public MqttBrokerPool mqttBrokerPool(@Value("${mqtt.broker-urls}") List<String> brokerUrls,
                                         MqttConnectionSettings settings) {
        return new MqttBrokerPool(brokerUrls, settings);
    }
}