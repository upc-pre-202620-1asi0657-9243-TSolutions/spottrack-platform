package com.spottrack.platform.monitoring.infrastructure.messaging.mqtt.translation;

import com.spottrack.platform.shared.application.result.ApplicationError;
import com.spottrack.platform.shared.application.result.Result;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class DefaultSensorPayloadTranslatorTest {

    private final DefaultSensorPayloadTranslator translator = new DefaultSensorPayloadTranslator(JsonMapper.builder().build());

    @Test
    void profileIsDefault() {
        assertEquals("default", translator.profile());
    }

    @Test
    void translatesCamelCasePayload() {
        var result = translator.translate(
                "{\"deviceId\":\"dev-1\",\"signalType\":\"MOTION\",\"durationMs\":1500,\"timestamp\":\"2026-11-13T10:00:00Z\"}");

        assertEquals(new TelemetrySignal("dev-1", "MOTION", 1500, "2026-11-13T10:00:00Z"), success(result));
    }

    @Test
    void translatesSnakeCasePayload() {
        var result = translator.translate(
                "{\"device_id\":\"dev-1\",\"signal_type\":\"MOTION\",\"duration_ms\":1500,\"timestamp\":\"2026-11-13T10:00:00Z\"}");

        assertEquals(new TelemetrySignal("dev-1", "MOTION", 1500, "2026-11-13T10:00:00Z"), success(result));
    }

    @Test
    void durationDefaultsToZeroWhenMissing() {
        var result = translator.translate("{\"deviceId\":\"dev-1\",\"signalType\":\"MOTION\"}");

        assertEquals(0, success(result).durationMs());
    }

    @Test
    void ignoresUnknownFields() {
        var result = translator.translate("{\"deviceId\":\"dev-1\",\"battery\":87}");

        assertEquals("dev-1", success(result).deviceId());
    }

    @Test
    void invalidJsonIsAValidationFailure() {
        var result = translator.translate("not a json");

        assertEquals("VALIDATION_ERROR", failure(result).code());
    }

    @Test
    void emptyPayloadIsAValidationFailure() {
        assertEquals("VALIDATION_ERROR", failure(translator.translate("")).code());
        assertEquals("VALIDATION_ERROR", failure(translator.translate("null")).code());
    }

    private static TelemetrySignal success(Result<TelemetrySignal, ApplicationError> result) {
        assertInstanceOf(Result.Success.class, result);
        return result.toOptional().orElseThrow();
    }

    private static ApplicationError failure(Result<TelemetrySignal, ApplicationError> result) {
        assertInstanceOf(Result.Failure.class, result);
        return ((Result.Failure<TelemetrySignal, ApplicationError>) result).error();
    }
}
