package com.spottrack.platform.monitoring.infrastructure.messaging.mqtt;

import org.eclipse.paho.client.mqttv3.MqttConnectOptions;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.security.cert.CertificateFactory;
import java.util.Base64;

/**
 * Credentials and TLS trust shared by every broker connection (Iteration 5, QA-05).
 * <p>
 * The brokers reject anonymous clients and only accept TLS on 8883, so the backend
 * connects with its service account and trusts the private CA that signed the
 * brokers' certificate. Empty values keep the old behaviour (anonymous, plain TCP)
 * for a local broker.
 *
 * @param username       MQTT service account of the backend ({@code MQTT_USERNAME})
 * @param password       its password ({@code MQTT_PASSWORD})
 * @param caCertificate  PEM of the brokers' CA, raw or base64 ({@code MQTT_CA_CERT})
 */
public record MqttConnectionSettings(String username, String password, String caCertificate) {

    /** Applies credentials and, for {@code ssl://} brokers, the TLS trust to the options. */
    void applyTo(MqttConnectOptions options, String brokerUrl) {
        if (username != null && !username.isBlank()) {
            options.setUserName(username);
            options.setPassword(password == null ? new char[0] : password.toCharArray());
        }
        if (brokerUrl.startsWith("ssl://") && caCertificate != null && !caCertificate.isBlank()) {
            options.setSocketFactory(socketFactory());
            // The certificate lists the brokers' IPs, so the host can be verified.
            options.setHttpsHostnameVerificationEnabled(true);
        }
    }

    private SSLSocketFactory socketFactory() {
        try {
            var certificate = CertificateFactory.getInstance("X.509")
                    .generateCertificate(new ByteArrayInputStream(pem()));
            var trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            trustStore.load(null, null);
            trustStore.setCertificateEntry("spottrack-mqtt-ca", certificate);

            var trustManagers = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagers.init(trustStore);

            var context = SSLContext.getInstance("TLSv1.2");
            context.init(null, trustManagers.getTrustManagers(), null);
            return context.getSocketFactory();
        } catch (Exception e) {
            throw new IllegalStateException("Invalid MQTT CA certificate (MQTT_CA_CERT)", e);
        }
    }

    /** Accepts the PEM as is, or base64 encoded (how Terraform passes it as one env line). */
    private byte[] pem() {
        var value = caCertificate.trim();
        if (value.startsWith("-----BEGIN")) return value.getBytes(StandardCharsets.UTF_8);
        return Base64.getDecoder().decode(value);
    }
}