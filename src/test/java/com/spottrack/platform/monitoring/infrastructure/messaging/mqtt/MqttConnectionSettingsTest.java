package com.spottrack.platform.monitoring.infrastructure.messaging.mqtt;

import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.junit.jupiter.api.Test;

import javax.net.ssl.SSLSocketFactory;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MqttConnectionSettingsTest {

    // Self-signed test CA, only used to build the trust store.
    private static final String CA_PEM = """
            -----BEGIN CERTIFICATE-----
            MIIBeDCCAR6gAwIBAgIUGYLYn17ns0IH3DwYJxNi52hxHgkwCgYIKoZIzj0EAwIw
            ITEfMB0GA1UEAwwWU3BvdFRyYWNrIHRlc3QgTVFUVCBDQTAgFw0yNjExMTQxNTUz
            NDRaGA8yMTI2MTAyMTE1NTM0NFowITEfMB0GA1UEAwwWU3BvdFRyYWNrIHRlc3Qg
            TVFUVCBDQTBZMBMGByqGSM49AgEGCCqGSM49AwEHA0IABGw0D6yvxMo1ySboi1JB
            FpmwtmdY7v76Z6TpborpH+DyrCR/uCJkUl2O6cl1e3gmVXHvEkSY73E5Z0bVWGqb
            /Z2jMjAwMB0GA1UdDgQWBBT0sPk5r9dyTvYxJcKLxnmiqXRjXjAPBgNVHRMBAf8E
            BTADAQH/MAoGCCqGSM49BAMCA0gAMEUCIQCllutOvtc7jJAYbd329jCXB6wKEDu0
            7FQZr4fzX17n/QIgFbMmLUwvKi7hws8fr7317QOkPZ1VDRI7/6keBuPqXNY=
            -----END CERTIFICATE-----
            """;

    private static final String TLS_BROKER = "ssl://10.128.0.2:8883";
    private static final String PLAIN_BROKER = "tcp://localhost:1883";

    @Test
    void emptySettingsKeepAnonymousPlainTcp() {
        var options = applied(new MqttConnectionSettings("", "", ""), PLAIN_BROKER);

        assertNull(options.getUserName());
        assertNull(options.getSocketFactory());
    }

    @Test
    void usernameAndPasswordAreSentToTheBroker() {
        var options = applied(new MqttConnectionSettings("spottrack-backend", "s3cret", ""), PLAIN_BROKER);

        assertEquals("spottrack-backend", options.getUserName());
        assertArrayEquals("s3cret".toCharArray(), options.getPassword());
    }

    @Test
    void missingPasswordIsSentAsEmpty() {
        var options = applied(new MqttConnectionSettings("spottrack-backend", null, ""), PLAIN_BROKER);

        assertArrayEquals(new char[0], options.getPassword());
    }

    @Test
    void sslBrokerTrustsTheCaAndVerifiesTheHost() {
        var options = applied(new MqttConnectionSettings("spottrack-backend", "s3cret", CA_PEM), TLS_BROKER);

        assertInstanceOf(SSLSocketFactory.class, options.getSocketFactory());
        assertTrue(options.isHttpsHostnameVerificationEnabled());
    }

    @Test
    void caCanBePassedBase64Encoded() {
        var base64 = Base64.getEncoder().encodeToString(CA_PEM.getBytes(StandardCharsets.UTF_8));

        var options = applied(new MqttConnectionSettings("spottrack-backend", "s3cret", base64), TLS_BROKER);

        assertInstanceOf(SSLSocketFactory.class, options.getSocketFactory());
    }

    @Test
    void plainTcpBrokerIgnoresTheCa() {
        var options = applied(new MqttConnectionSettings("spottrack-backend", "s3cret", CA_PEM), PLAIN_BROKER);

        assertNull(options.getSocketFactory());
    }

    @Test
    void invalidCaFailsFast() {
        var settings = new MqttConnectionSettings("spottrack-backend", "s3cret", "not-a-certificate");

        assertThrows(IllegalStateException.class, () -> settings.applyTo(new MqttConnectOptions(), TLS_BROKER));
    }

    private static MqttConnectOptions applied(MqttConnectionSettings settings, String brokerUrl) {
        var options = new MqttConnectOptions();
        settings.applyTo(options, brokerUrl);
        return options;
    }
}
