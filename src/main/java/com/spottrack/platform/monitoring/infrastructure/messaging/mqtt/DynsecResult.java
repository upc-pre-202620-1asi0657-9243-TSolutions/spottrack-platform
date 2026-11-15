package com.spottrack.platform.monitoring.infrastructure.messaging.mqtt;

/**
 * Outcome of one dynamic-security command.
 *
 * @param command the command name echoed by the broker
 * @param error   the broker's error text, or {@code null} when the command succeeded
 */
record DynsecResult(String command, String error) {
    static final String CLIENT_NOT_FOUND = "Client not found";

    boolean ok() {
        return error == null;
    }
}
