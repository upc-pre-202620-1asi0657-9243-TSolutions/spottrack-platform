package com.spottrack.platform.monitoring.infrastructure.messaging.mqtt.translation;

import com.spottrack.platform.shared.application.result.Result;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.jackson.autoconfigure.JacksonAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;

class SensorPayloadTranslationWiringTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(JacksonAutoConfiguration.class))
            .withUserConfiguration(DefaultSensorPayloadTranslator.class, SensorPayloadTranslatorSelector.class);

    @Test
    void selectorResolvesTheDefaultTranslatorBackedBySpringsJacksonMapper() {
        contextRunner.run(context -> {
            assertNull(context.getStartupFailure());

            var selection = context.getBean(SensorPayloadTranslatorSelector.class).select(null);

            assertInstanceOf(Result.Success.class, selection);
            var translator = selection.toOptional().orElseThrow();
            assertEquals("default", translator.profile());

            var translation = translator.translate("{\"deviceId\":\"dev-1\",\"signalType\":\"MOTION\"}");

            assertInstanceOf(Result.Success.class, translation);
            assertEquals(new TelemetrySignal("dev-1", "MOTION", 0, null), translation.toOptional().orElseThrow());
        });
    }
}
