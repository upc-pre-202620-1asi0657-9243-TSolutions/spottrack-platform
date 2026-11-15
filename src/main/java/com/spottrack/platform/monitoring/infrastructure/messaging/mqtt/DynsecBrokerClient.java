package com.spottrack.platform.monitoring.infrastructure.messaging.mqtt;

import java.util.List;

/** Sends dynamic-security commands to one broker and waits for its answer. */
interface DynsecBrokerClient {

    /**
     * Sends all commands in one message, so the broker applies them in order.
     *
     * @return one result per command, in the same order
     * @throws DynsecTransportException when the broker is unreachable, refuses the admin account or does not answer
     */
    List<DynsecResult> execute(String brokerUrl, List<DynsecCommand> commands) throws DynsecTransportException;
}
