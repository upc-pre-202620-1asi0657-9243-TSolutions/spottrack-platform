package com.spottrack.platform.monitoring.infrastructure.messaging.mqtt.translation;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.spottrack.platform.shared.application.result.ApplicationError;
import com.spottrack.platform.shared.application.result.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;

/**
 * Compact payload of the alternative emulator: {@code {"dev":"…","sig":"mv|hb","dur":1.5,"ts":"2026-11-15T12:00:00Z"}}.
 * {@code dev} and {@code sig} are required, {@code dur} is in seconds (decimals allowed) and defaults to 0,
 * {@code ts} is ISO-8601 with offset and defaults to the server clock when absent.
 */
@Component
public class CompactSensorPayloadTranslator implements SensorPayloadTranslator {

    public static final String PROFILE_NAME = "compact-emulator";

    private static final String FIELD = "sensorPayload";

    private final ObjectMapper objectMapper;
    private final Clock clock;

    @Autowired
    public CompactSensorPayloadTranslator(ObjectMapper objectMapper) {
        this(objectMapper, Clock.systemUTC());
    }

    CompactSensorPayloadTranslator(ObjectMapper objectMapper, Clock clock) {
        this.objectMapper = objectMapper;
        this.clock = clock;
    }

    @Override
    public String profile() {
        return PROFILE_NAME;
    }

    @Override
    public Result<TelemetrySignal, ApplicationError> translate(String payload) {
        CompactMessage message;
        try {
            message = objectMapper.readValue(payload, CompactMessage.class);
        } catch (JacksonException e) {
            return invalid(e.getOriginalMessage());
        }
        if (message == null) {
            return invalid("Payload is empty");
        }
        if (message.dev() == null || message.dev().isBlank()) {
            return invalid("Field 'dev' is required");
        }
        var signalType = signalTypeOf(message.sig());
        if (signalType == null) {
            return invalid("Field 'sig' must be 'mv' or 'hb'");
        }
        var duration = message.dur() == null ? 0.0 : message.dur();
        if (!Double.isFinite(duration) || duration < 0) {
            return invalid("Field 'dur' must be a non-negative number of seconds");
        }
        String timestamp;
        try {
            timestamp = message.ts() == null || message.ts().isBlank()
                    ? clock.instant().toString()
                    : OffsetDateTime.parse(message.ts()).toInstant().toString();
        } catch (DateTimeParseException e) {
            return invalid("Field 'ts' must be an ISO-8601 date-time with offset");
        }
        return Result.success(new TelemetrySignal(
                message.dev(), signalType, Math.round(duration * 1000), timestamp));
    }

    private static String signalTypeOf(String sig) {
        if ("mv".equals(sig)) return "MOTION";
        if ("hb".equals(sig)) return "HEARTBEAT";
        return null;
    }

    private static Result<TelemetrySignal, ApplicationError> invalid(String reason) {
        return Result.failure(ApplicationError.validationError(FIELD, reason));
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record CompactMessage(
            @JsonProperty("dev") String dev,
            @JsonProperty("sig") String sig,
            @JsonProperty("dur") Double dur,
            @JsonProperty("ts") String ts
    ) {
    }
}
