package com.spottrack.platform.monitoring.infrastructure.messaging.mqtt;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class MqttConfig {

    /**
     * One subscriber connection per broker of the cluster. Falls back to the
     * single {@code mqtt.broker-url} when {@code mqtt.broker-urls} is not set.
     */
    @Bean
    public MqttBrokerPool mqttBrokerPool(@Value("${mqtt.broker-urls}") List<String> brokerUrls) {
        return new MqttBrokerPool(brokerUrls);
    }
}
