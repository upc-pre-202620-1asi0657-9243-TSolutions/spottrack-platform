package com.spottrack.platform.monitoring.application.ports;

import java.time.Duration;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * @param ttl            how long a QR stays valid while it has not been scanned ({@code pairing.ttl})
 * @param publicEndpoint {@code host:port} of the brokers' balancer as seen by devices ({@code mqtt.public-endpoint})
 * @param caSha256       SHA-256 of the broker CA in DER, hex ({@code mqtt.ca-sha256})
 */
public record SensorPairingSettings(Duration ttl, String publicEndpoint, String caSha256) {
    private static final Pattern SHA256_HEX = Pattern.compile("[0-9a-f]{64}");

    /** @throws IllegalStateException when the pairing is not configured well enough to issue a usable QR */
    public void requireConfigured() {
        if (ttl == null || ttl.isZero() || ttl.isNegative()) {
            throw new IllegalStateException("pairing.ttl must be a positive duration");
        }
        host();
        port();
        if (normalizedCaSha256().isEmpty()) {
            throw new IllegalStateException("mqtt.ca-sha256 is not configured");
        }
        if (!SHA256_HEX.matcher(normalizedCaSha256()).matches()) {
            throw new IllegalStateException("mqtt.ca-sha256 must be 64 hexadecimal characters");
        }
    }

    public String host() {
        if (endpoint().isEmpty()) {
            throw new IllegalStateException("mqtt.public-endpoint is not configured (expected host:port)");
        }
        var separator = endpoint().lastIndexOf(':');
        var host = separator < 0 ? "" : endpoint().substring(0, separator).trim();
        if (host.isEmpty()) {
            throw new IllegalStateException("mqtt.public-endpoint must be host:port");
        }
        return host;
    }

    public int port() {
        var separator = endpoint().lastIndexOf(':');
        try {
            var port = Integer.parseInt(endpoint().substring(separator + 1).trim());
            if (separator >= 0 && port > 0 && port <= 65535) return port;
        } catch (NumberFormatException ignored) {
            // falls through to the error below
        }
        throw new IllegalStateException("mqtt.public-endpoint must be host:port");
    }

    /** Lower case without the colons of the {@code openssl} output. */
    public String normalizedCaSha256() {
        return caSha256 == null ? "" : caSha256.trim().replace(":", "").toLowerCase(Locale.ROOT);
    }

    private String endpoint() {
        return publicEndpoint == null ? "" : publicEndpoint.trim();
    }
}
