package com.spottrack.platform.monitoring.infrastructure.messaging.mqtt.translation;

import com.spottrack.platform.monitoring.infrastructure.messaging.mqtt.SensorSignalMessage;
import com.spottrack.platform.shared.application.result.ApplicationError;
import com.spottrack.platform.shared.application.result.Result;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.ObjectMapper;

/** SpotTrack's own payload format (camelCase keys, snake_case still accepted). */
@Component
public class DefaultSensorPayloadTranslator implements SensorPayloadTranslator {

    public static final String PROFILE = "default";

    private final ObjectMapper objectMapper;

    public DefaultSensorPayloadTranslator(ObjectMapper objectMapper) {
        // Jackson 3 rejects a missing primitive by default; devices may omit durationMs, which must read as 0.
        this.objectMapper = objectMapper.rebuild()
                .disable(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES)
                .build();
    }

    @Override
    public String profile() {
        return PROFILE;
    }

    @Override
    public Result<TelemetrySignal, ApplicationError> translate(String payload) {
        try {
            var message = objectMapper.readValue(payload, SensorSignalMessage.class);
            if (message == null) {
                return Result.failure(ApplicationError.validationError("sensorPayload", "Payload is empty"));
            }
            return Result.success(new TelemetrySignal(
                    message.deviceId(), message.signalType(), message.durationMs(), message.timestamp()));
        } catch (JacksonException e) {
            return Result.failure(ApplicationError.validationError("sensorPayload", e.getOriginalMessage()));
        }
    }
}
