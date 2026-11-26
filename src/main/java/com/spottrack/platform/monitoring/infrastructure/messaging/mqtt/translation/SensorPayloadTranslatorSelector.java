package com.spottrack.platform.monitoring.infrastructure.messaging.mqtt.translation;

import com.spottrack.platform.monitoring.application.ports.PayloadProfileCatalog;
import com.spottrack.platform.shared.application.result.ApplicationError;
import com.spottrack.platform.shared.application.result.Result;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class SensorPayloadTranslatorSelector implements PayloadProfileCatalog {

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
                ? PayloadProfileCatalog.DEFAULT_PROFILE
                : payloadProfile;
    }

    @Override
    public boolean isSupported(String profile) {
        return translatorsByProfile.containsKey(resolveProfile(profile));
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
