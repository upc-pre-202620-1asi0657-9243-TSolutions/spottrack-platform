package com.spottrack.platform.monitoring.infrastructure.pairing;

import com.spottrack.platform.monitoring.application.ports.SensorPairingSettings;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.convert.DurationStyle;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SensorPairingConfiguration {

    @Bean
    public SensorPairingSettings sensorPairingSettings(
            @Value("${pairing.ttl:15m}") String ttl,
            @Value("${mqtt.public-endpoint:}") String publicEndpoint,
            @Value("${mqtt.ca-sha256:}") String caSha256) {
        return new SensorPairingSettings(DurationStyle.detectAndParse(ttl), publicEndpoint, caSha256);
    }
}
