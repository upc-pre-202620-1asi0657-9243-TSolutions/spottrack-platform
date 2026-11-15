package com.spottrack.platform.monitoring.infrastructure.pairing;

import com.spottrack.platform.monitoring.application.ports.BrokerCredentialProvisioner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/** Does not touch any broker: it only logs the device id, never the secret. */
@Component
@ConditionalOnProperty(name = "pairing.provisioner.mode", havingValue = "noop", matchIfMissing = true)
public class LoggingBrokerCredentialProvisioner implements BrokerCredentialProvisioner {
    private static final Logger log = LoggerFactory.getLogger(LoggingBrokerCredentialProvisioner.class);

    @Override
    public void provision(String deviceId, String secret) {
        log.info("[noop provisioner] broker credential NOT created for device_id={}", deviceId);
    }

    @Override
    public void revoke(String deviceId) {
        log.info("[noop provisioner] broker credential NOT removed for device_id={}", deviceId);
    }
}
