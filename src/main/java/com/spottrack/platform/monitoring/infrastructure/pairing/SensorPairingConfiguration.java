package com.spottrack.platform.monitoring.infrastructure.pairing;

import com.spottrack.platform.monitoring.application.ports.SensorPairingSettings;
import com.spottrack.platform.monitoring.infrastructure.messaging.mqtt.MqttConnectionSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.convert.DurationStyle;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SensorPairingConfiguration {
    private static final Logger log = LoggerFactory.getLogger(SensorPairingConfiguration.class);

    /**
     * {@code mqtt.ca-sha256} wins when set; otherwise it is derived from {@code mqtt.ca-cert}
     * so the pin can never drift from the CA the backend itself trusts.
     */
    @Bean
    public SensorPairingSettings sensorPairingSettings(
            @Value("${pairing.ttl:15m}") String ttl,
            @Value("${mqtt.public-endpoint:}") String publicEndpoint,
            @Value("${mqtt.ca-sha256:}") String caSha256,
            @Value("${mqtt.ca-cert:}") String caCertificate) {
        return new SensorPairingSettings(DurationStyle.detectAndParse(ttl), publicEndpoint,
                caSha256.isBlank() ? deriveCaSha256(caCertificate) : caSha256);
    }

    private static String deriveCaSha256(String caCertificate) {
        if (caCertificate.isBlank()) return "";
        try {
            return MqttConnectionSettings.caSha256Of(caCertificate);
        } catch (IllegalArgumentException e) {
            log.warn("mqtt.ca-sha256 could not be derived from mqtt.ca-cert, pairing will refuse to issue: {}", e.getMessage());
            return "";
        }
    }
}
