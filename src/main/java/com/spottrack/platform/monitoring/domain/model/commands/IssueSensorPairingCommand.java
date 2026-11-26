package com.spottrack.platform.monitoring.domain.model.commands;

/**
 * @param payloadProfile profile to assign to the sensor while pairing it; {@code null} or blank keeps the one it has.
 */
public record IssueSensorPairingCommand(String motionSensorId, String payloadProfile) {

    public IssueSensorPairingCommand(String motionSensorId) {
        this(motionSensorId, null);
    }
}
