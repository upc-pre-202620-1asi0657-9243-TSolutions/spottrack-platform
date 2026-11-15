package com.spottrack.platform.monitoring.application.ports;

/**
 * Creates and removes the MQTT credential of a device on the broker cluster.
 * <p>
 * The brokers are independent and a load balancer routes a device to any of them,
 * so both operations are all-or-nothing: if one throws, no broker may keep the
 * change. {@link #provision} replaces any previous secret of the same device.
 * Exception messages must never contain the secret.
 */
public interface BrokerCredentialProvisioner {
    void provision(String deviceId, String secret);

    void revoke(String deviceId);
}
