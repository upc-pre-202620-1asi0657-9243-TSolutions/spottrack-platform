package com.spottrack.platform.monitoring.infrastructure.messaging.mqtt.translation;

import com.spottrack.platform.shared.application.result.ApplicationError;
import com.spottrack.platform.shared.application.result.Result;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Iteration 6 Interoperability Contract Tests (TA-12 / DDR-16).
 * Validates sensor payload translators against sample contract payloads versioned in src/test/resources/contracts/telemetry/.
 */
class SensorPayloadTranslatorContractTest {

    private SensorPayloadTranslator translator;

    @BeforeEach
    void setUp() {
        translator = new DefaultSensorPayloadTranslator(JsonMapper.builder().build());
    }

    @Test
    @DisplayName("Valid camelCase payload in contract resources satisfies v1 canonical contract")
    void validCamelCasePayloadSatisfiesV1Contract() {
        var payload = loadContractPayload("contracts/telemetry/default-v1-valid-camelcase.json");

        var result = translator.translate(payload);

        var signal = assertSuccess(result);
        assertEquals("dev-sensor-001", signal.deviceId());
        assertEquals("MOTION", signal.signalType());
        assertEquals(1500L, signal.durationMs());
        assertEquals("2026-11-15T12:00:00Z", signal.timestamp());
        assertEquals(1, signal.schemaVersion());
    }

    @Test
    @DisplayName("Valid snake_case payload in contract resources satisfies v1 canonical contract")
    void validSnakeCasePayloadSatisfiesV1Contract() {
        var payload = loadContractPayload("contracts/telemetry/default-v1-valid-snakecase.json");

        var result = translator.translate(payload);

        var signal = assertSuccess(result);
        assertEquals("dev-sensor-002", signal.deviceId());
        assertEquals("MOTION", signal.signalType());
        assertEquals(2000L, signal.durationMs());
        assertEquals("2026-11-15T12:05:00Z", signal.timestamp());
        assertEquals(1, signal.schemaVersion());
    }

    @Test
    @DisplayName("Legacy payload without schema_version satisfies v1 canonical contract defaulting to version 1")
    void legacyPayloadWithoutVersionSatisfiesV1ContractDefaultingVersion() {
        var payload = loadContractPayload("contracts/telemetry/default-v1-legacy-no-version.json");

        var result = translator.translate(payload);

        var signal = assertSuccess(result);
        assertEquals("dev-sensor-003", signal.deviceId());
        assertEquals("MOTION", signal.signalType());
        assertEquals(800L, signal.durationMs());
        assertEquals("2026-11-15T12:10:00Z", signal.timestamp());
        assertEquals(1, signal.schemaVersion());
    }

    @Test
    @DisplayName("Tolerant reader contract payload with extra unknown fields preserves canonical attributes and schema version")
    void tolerantPayloadWithExtraFieldsSatisfiesV1ContractIgnoringUnknowns() {
        var payload = loadContractPayload("contracts/telemetry/default-v1-tolerant-extra-fields.json");

        var result = translator.translate(payload);

        var signal = assertSuccess(result);
        assertEquals("dev-sensor-004", signal.deviceId());
        assertEquals("MOTION", signal.signalType());
        assertEquals(1100L, signal.durationMs());
        assertEquals("2026-11-15T12:15:00Z", signal.timestamp());
        assertEquals(1, signal.schemaVersion());
    }


    private String loadContractPayload(String resourcePath) {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(resourcePath)) {
            assertNotNull(is, "Contract sample file not found: " + resourcePath);
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new AssertionError("Failed to load contract sample from " + resourcePath, e);
        }
    }

    private static TelemetrySignal assertSuccess(Result<TelemetrySignal, ApplicationError> result) {
        assertInstanceOf(Result.Success.class, result);
        return result.toOptional().orElseThrow();
    }

    private static ApplicationError assertFailure(Result<TelemetrySignal, ApplicationError> result) {
        assertInstanceOf(Result.Failure.class, result);
        return ((Result.Failure<TelemetrySignal, ApplicationError>) result).error();
    }
}
