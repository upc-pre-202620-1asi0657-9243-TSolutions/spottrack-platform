package com.spottrack.platform.monitoring.infrastructure.messaging.mqtt.translation;

import com.spottrack.platform.shared.application.result.ApplicationError;
import com.spottrack.platform.shared.application.result.Result;

/**
 * Turns the raw MQTT payload of one device family into the common {@link TelemetrySignal}.
 * Each implementation is a Spring bean and is picked by {@link SensorPayloadTranslatorSelector}
 * through {@link #profile()}, so supporting a new payload format means adding a new bean.
 */
public interface SensorPayloadTranslator {

    String profile();

    Result<TelemetrySignal, ApplicationError> translate(String payload);
}
