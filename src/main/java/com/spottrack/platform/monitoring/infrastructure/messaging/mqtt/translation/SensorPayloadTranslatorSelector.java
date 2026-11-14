package com.spottrack.platform.monitoring.infrastructure.messaging.mqtt.translation;

import com.spottrack.platform.shared.application.result.ApplicationError;
import com.spottrack.platform.shared.application.result.Result;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class SensorPayloadTranslatorSelector {

    private final Map<String, SensorPayloadTranslator> translatorsByProfile = new HashMap<>();

    public SensorPayloadTranslatorSelector(List<SensorPayloadTranslator> translators) {
        for (var translator : translators) {
            var previous = translatorsByProfile.put(translator.profile(), translator);
            if (previous != null) {
                throw new IllegalStateException("Duplicate sensor payload profile '%s': %s and %s".formatted(
                        translator.profile(), previous.getClass().getName(), translator.getClass().getName()));
            }
        }
    }

    /** A null or blank profile means the sensor uses SpotTrack's own format. */
    public static String resolveProfile(String payloadProfile) {
        return payloadProfile == null || payloadProfile.isBlank()
                ? DefaultSensorPayloadTranslator.PROFILE
                : payloadProfile;
    }

    public Result<SensorPayloadTranslator, ApplicationError> select(String payloadProfile) {
        var profile = resolveProfile(payloadProfile);
        var translator = translatorsByProfile.get(profile);
        if (translator == null) {
            return Result.failure(ApplicationError.validationError(
                    "payloadProfile", "Unknown sensor payload profile '%s'".formatted(profile)));
        }
        return Result.success(translator);
    }
}
