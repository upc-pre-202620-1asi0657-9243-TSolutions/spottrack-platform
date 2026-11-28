package com.spottrack.platform.monitoring.application.ports;

/**
 * Creates and removes the MQTT credential of a device on the broker cluster.
 * <p>
 * The brokers are independent and a load balancer routes a device to any of them,
 * so a credential must exist on all of them or on none.
 * <ul>
 *   <li>{@link #provision} replaces any previous secret of the same device. If it
 *   throws, the credential has been removed from every broker it could reach (a
 *   device that was being re-issued is left without credential, never with a mixed
 *   one).</li>
 *   <li>{@link #revoke} is idempotent: a device that has no credential is not an
 *   error. It tries every broker even when one fails and throws at the end, so a
 *   later retry can finish the job.</li>
 * </ul>
 * Exception messages must never contain the secret.
 */
public interface BrokerCredentialProvisioner {

    /**
     * Bean name shared by every implementation. They are mutually exclusive on
     * {@code pairing.provisioner.mode}, so exactly one of them is registered under it.
     */
    String BEAN_NAME = "brokerCredentialProvisioner";
    void provision(String deviceId, String secret);

    void revoke(String deviceId);
}
