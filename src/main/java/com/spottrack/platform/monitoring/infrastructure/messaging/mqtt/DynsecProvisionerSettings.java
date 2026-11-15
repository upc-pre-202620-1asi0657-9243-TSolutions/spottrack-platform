package com.spottrack.platform.monitoring.infrastructure.messaging.mqtt;

import java.time.Duration;
import java.util.List;

/**
 * @param brokerUrls   every broker of the cluster; a credential must exist on all of them ({@code mqtt.broker-urls})
 * @param sensorRole   dynamic-security role granted to each device ({@code pairing.dynsec.sensor-role})
 * @param maxAttempts  attempts per broker on transport errors ({@code pairing.dynsec.max-attempts})
 * @param totalTimeout deadline of one fan-out to all brokers, retries included ({@code pairing.dynsec.total-timeout})
 * @param retryDelay   pause between two attempts on the same broker
 */
record DynsecProvisionerSettings(List<String> brokerUrls, String sensorRole, int maxAttempts, Duration totalTimeout,
                                 Duration retryDelay) {
    DynsecProvisionerSettings {
        brokerUrls = List.copyOf(brokerUrls);
    }
}
