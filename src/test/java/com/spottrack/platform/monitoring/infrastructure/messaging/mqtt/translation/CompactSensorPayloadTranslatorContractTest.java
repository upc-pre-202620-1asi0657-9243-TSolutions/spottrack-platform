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
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Interoperability contract tests (QA-06) for the compact-emulator profile (dev, sig, dur, ts),
 * against the sample payloads versioned in src/test/resources/contracts/telemetry/.
 */
class CompactSensorPayloadTranslatorContractTest {

    private static final Instant NOW = Instant.parse("2026-11-15T18:30:00Z");

    private SensorPayloadTranslator translator;

    @BeforeEach
    void setUp() {
        translator = new CompactSensorPayloadTranslator(
                JsonMapper.builder().build(), Clock.fixed(NOW, ZoneOffset.UTC));
    }

    @Test
    @DisplayName("Profile id is compact-emulator")
    void profileIdIsCompactEmulator() {
        assertEquals("compact-emulator", translator.profile());
    }

    @Test
    @DisplayName("Motion payload maps dev, sig, dur (seconds) and ts to the canonical contract")
    void motionPayloadSatisfiesCanonicalContract() {
        var signal = assertSuccess(translator.translate(load("compact-emulator-v1-valid-motion.json")));

        assertEquals(new TelemetrySignal("alt-sensor-001", "MOTION", 1500L, "2026-11-15T12:00:00Z", 1), signal);
    }

    @Test
    @DisplayName("Heartbeat payload maps hb to HEARTBEAT and normalizes the timestamp offset to UTC")
    void heartbeatPayloadWithOffsetIsNormalizedToUtc() {
        var signal = assertSuccess(translator.translate(load("compact-emulator-v1-valid-heartbeat-offset.json")));

        assertEquals(new TelemetrySignal("alt-sensor-002", "HEARTBEAT", 0L, "2026-11-15T12:05:00Z", 1), signal);
    }

    @Test
    @DisplayName("Payload without dur and ts defaults the duration to 0 and the timestamp to the server clock")
    void minimalPayloadUsesDefaults() {
        var signal = assertSuccess(translator.translate(load("compact-emulator-v1-minimal.json")));

        assertEquals(new TelemetrySignal("alt-sensor-003", "MOTION", 0L, NOW.toString(), 1), signal);
    }

    @Test
    @DisplayName("Tolerant reader ignores unknown fields and keeps the canonical attributes")
    void tolerantPayloadIgnoresUnknownFields() {
        var signal = assertSuccess(translator.translate(load("compact-emulator-v1-tolerant-extra-fields.json")));

        assertEquals(new TelemetrySignal("alt-sensor-004", "MOTION", 800L, "2026-11-15T12:15:00Z", 1), signal);
    }

    @Test
    @DisplayName("Decimal seconds are rounded to the nearest millisecond")
    void decimalSecondsAreRoundedToMilliseconds() {
        var signal = assertSuccess(translator.translate("{\"dev\":\"d\",\"sig\":\"mv\",\"dur\":0.0006}"));

        assertEquals(1L, signal.durationMs());
    }

    @Test
    @DisplayName("Integer seconds are converted to milliseconds")
    void integerSecondsAreConvertedToMilliseconds() {
        var signal = assertSuccess(translator.translate("{\"dev\":\"d\",\"sig\":\"mv\",\"dur\":3}"));

        assertEquals(3000L, signal.durationMs());
    }

    @Test
    @DisplayName("Malformed payload returns a validation error without throwing")
    void malformedPayloadReturnsValidationError() {
        assertValidationError(load("compact-emulator-v1-invalid-malformed.json"));
    }

    @Test
    @DisplayName("Payload without dev is rejected")
    void missingDevIsRejected() {
        var error = assertValidationError(load("compact-emulator-v1-invalid-missing-dev.json"));

        assertTrue(error.details().contains("dev"));
    }

    @Test
    @DisplayName("Blank dev is rejected")
    void blankDevIsRejected() {
        assertValidationError("{\"dev\":\"  \",\"sig\":\"mv\"}");
    }

    @Test
    @DisplayName("Payload without sig is rejected")
    void missingSigIsRejected() {
        assertValidationError("{\"dev\":\"d\"}");
    }

    @Test
    @DisplayName("Unknown sig value is rejected")
    void unknownSigIsRejected() {
        var error = assertValidationError(load("compact-emulator-v1-invalid-unknown-sig.json"));

        assertTrue(error.details().contains("sig"));
    }

    @Test
    @DisplayName("Negative dur is rejected")
    void negativeDurIsRejected() {
        assertValidationError("{\"dev\":\"d\",\"sig\":\"mv\",\"dur\":-1}");
    }

    @Test
    @DisplayName("Non numeric dur is rejected")
    void nonNumericDurIsRejected() {
        assertValidationError("{\"dev\":\"d\",\"sig\":\"mv\",\"dur\":\"abc\"}");
    }

    @Test
    @DisplayName("Timestamp that is not ISO-8601 is rejected")
    void invalidTimestampIsRejected() {
        var error = assertValidationError(load("compact-emulator-v1-invalid-timestamp.json"));

        assertTrue(error.details().contains("ts"));
    }

    @Test
    @DisplayName("Empty string payload returns a validation error without throwing")
    void emptyStringPayloadReturnsValidationError() {
        assertValidationError("");
    }

    @Test
    @DisplayName("Literal null payload returns a validation error without throwing")
    void literalNullPayloadReturnsValidationError() {
        assertValidationError("null");
    }

    @Test
    @DisplayName("A JSON array payload returns a validation error without throwing")
    void arrayPayloadReturnsValidationError() {
        assertValidationError("[]");
    }

    @Test
    @DisplayName("The canonical default format is not accepted by this profile")
    void defaultFormatIsNotAcceptedByThisProfile() {
        assertValidationError("{\"deviceId\":\"d\",\"signalType\":\"MOTION\",\"durationMs\":800}");
    }

    private ApplicationError assertValidationError(String payload) {
        var result = assertDoesNotThrow(() -> translator.translate(payload));
        assertInstanceOf(Result.Failure.class, result);
        var error = ((Result.Failure<TelemetrySignal, ApplicationError>) result).error();
        assertEquals("VALIDATION_ERROR", error.code());
        assertNotNull(error.message());
        return error;
    }

    private static TelemetrySignal assertSuccess(Result<TelemetrySignal, ApplicationError> result) {
        assertInstanceOf(Result.Success.class, result);
        return result.toOptional().orElseThrow();
    }

    private String load(String fileName) {
        var resourcePath = "contracts/telemetry/" + fileName;
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(resourcePath)) {
            assertNotNull(is, "Contract sample file not found: " + resourcePath);
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new AssertionError("Failed to load contract sample from " + resourcePath, e);
        }
    }
}
