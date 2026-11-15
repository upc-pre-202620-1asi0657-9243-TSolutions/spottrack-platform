package com.spottrack.platform.monitoring.infrastructure.messaging.mqtt;

/** The broker could not be reached or did not answer in time. The commands may or may not have been applied. */
class DynsecTransportException extends Exception {
    DynsecTransportException(String message) {
        super(message);
    }

    DynsecTransportException(String message, Throwable cause) {
        super(message, cause);
    }
}
