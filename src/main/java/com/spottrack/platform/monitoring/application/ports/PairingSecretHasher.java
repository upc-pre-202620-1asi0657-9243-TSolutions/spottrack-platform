package com.spottrack.platform.monitoring.application.ports;

public interface PairingSecretHasher {
    String hash(String secret);
}
