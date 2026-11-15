package com.spottrack.platform.monitoring.infrastructure.messaging.mqtt;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.convert.DurationStyle;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Real credential provisioning ({@code pairing.provisioner.mode=dynsec}).
 * <p>
 * Incomplete configuration fails the startup: silently falling back to a provisioner that
 * does nothing would hand out QR codes whose credentials the brokers never learn about.
 */
@Configuration
@ConditionalOnProperty(name = "pairing.provisioner.mode", havingValue = "dynsec")
public class DynsecProvisionerConfiguration {

    @Bean
    public DynamicSecurityBrokerCredentialProvisioner dynamicSecurityBrokerCredentialProvisioner(
            @Value("${mqtt.broker-urls:}") String brokerUrls,
            @Value("${mqtt.ca-cert:}") String caCertificate,
            @Value("${mqtt.dynsec.admin-username:}") String adminUsername,
            @Value("${mqtt.dynsec.admin-password:}") String adminPassword,
            @Value("${pairing.dynsec.sensor-role:sensor}") String sensorRole,
            @Value("${pairing.dynsec.connect-timeout:3s}") String connectTimeout,
            @Value("${pairing.dynsec.response-timeout:3s}") String responseTimeout,
            @Value("${pairing.dynsec.max-attempts:2}") int maxAttempts,
            @Value("${pairing.dynsec.total-timeout:15s}") String totalTimeout,
            @Value("${pairing.dynsec.retry-delay:200ms}") String retryDelay) {
        var urls = Arrays.stream(brokerUrls.split(",")).map(String::trim).filter(url -> !url.isEmpty()).distinct().toList();
        var problems = new ArrayList<String>();
        if (urls.isEmpty()) problems.add("mqtt.broker-urls is empty");
        if (urls.stream().anyMatch(url -> !url.startsWith("ssl://"))) {
            problems.add("mqtt.broker-urls must only contain ssl:// URLs (the admin password must not travel in clear)");
        }
        if (adminUsername.isBlank()) problems.add("mqtt.dynsec.admin-username is empty");
        if (adminPassword.isBlank()) problems.add("mqtt.dynsec.admin-password is empty");
        if (sensorRole.isBlank()) problems.add("pairing.dynsec.sensor-role is empty");
        if (maxAttempts < 1) problems.add("pairing.dynsec.max-attempts must be at least 1");
        if (caCertificate.isBlank()) {
            problems.add("mqtt.ca-cert is empty");
        } else {
            try {
                MqttConnectionSettings.caSha256Of(caCertificate);
            } catch (IllegalArgumentException e) {
                problems.add("mqtt.ca-cert is not a valid certificate");
            }
        }
        var connect = positive("pairing.dynsec.connect-timeout", connectTimeout, problems);
        var response = positive("pairing.dynsec.response-timeout", responseTimeout, problems);
        var total = positive("pairing.dynsec.total-timeout", totalTimeout, problems);
        var retry = DurationStyle.detectAndParse(retryDelay);
        if (!problems.isEmpty()) {
            throw new IllegalStateException("pairing.provisioner.mode=dynsec but the configuration is incomplete: "
                    + String.join("; ", problems));
        }

        var client = new PahoDynsecBrokerClient(new MqttConnectionSettings(adminUsername, adminPassword, caCertificate),
                connect, response);
        return new DynamicSecurityBrokerCredentialProvisioner(client,
                new DynsecProvisionerSettings(urls, sensorRole.trim(), maxAttempts, total, retry));
    }

    private static Duration positive(String property, String value, List<String> problems) {
        var duration = DurationStyle.detectAndParse(value);
        if (duration.isZero() || duration.isNegative()) {
            problems.add(property + " must be a positive duration");
        }
        return duration;
    }
}
