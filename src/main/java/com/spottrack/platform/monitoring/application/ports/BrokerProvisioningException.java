package com.spottrack.platform.monitoring.application.ports;

public class BrokerProvisioningException extends RuntimeException {
    public BrokerProvisioningException(String message) {
        super(message);
    }

    public BrokerProvisioningException(String message, Throwable cause) {
        super(message, cause);
    }
}
