package com.spottrack.platform.monitoring.infrastructure.messaging.mqtt;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Settings of the QA tools (emulator, latency benchmark). Every value comes from a system property
 * ({@code -Dqa01.mqtt.url=...}) or its environment variable ({@code QA01_MQTT_URL}); the property wins.
 * Nothing sensitive has a default and nothing is read from the repository.
 */
final class Qa01Settings {

    private Qa01Settings() {
    }

    static String optional(String name) {
        var value = System.getProperty(name);
        if (value == null || value.isBlank()) {
            value = System.getenv(environmentName(name));
        }
        return value == null || value.isBlank() ? null : value.trim();
    }

    static String required(String name) {
        var value = optional(name);
        if (value == null) {
            throw new IllegalStateException("Missing setting: pass -D%s=... or set %s".formatted(name, environmentName(name)));
        }
        return value;
    }

    static String orDefault(String name, String fallback) {
        var value = optional(name);
        return value == null ? fallback : value;
    }

    static long longOrDefault(String name, long fallback) {
        var value = optional(name);
        if (value == null) return fallback;
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            throw new IllegalStateException("Setting %s must be an integer, got '%s'".formatted(name, value), e);
        }
    }

    static boolean booleanOrDefault(String name, boolean fallback) {
        var value = optional(name);
        return value == null ? fallback : Boolean.parseBoolean(value);
    }

    /** Broker credentials and TLS trust ({@code qa01.mqtt.username}, {@code qa01.mqtt.password}, {@code qa01.mqtt.ca.file}); all optional. */
    static MqttConnectionSettings mqttConnection() {
        var caFile = optional("qa01.mqtt.ca.file");
        String caCertificate = null;
        if (caFile != null) {
            try {
                caCertificate = Files.readString(Path.of(caFile));
            } catch (IOException e) {
                throw new IllegalStateException("Cannot read the CA certificate file given in qa01.mqtt.ca.file", e);
            }
        }
        return new MqttConnectionSettings(optional("qa01.mqtt.username"), optional("qa01.mqtt.password"), caCertificate);
    }

    static String environmentName(String name) {
        return name.toUpperCase().replace('.', '_');
    }
}
