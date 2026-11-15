package com.spottrack.platform.monitoring.infrastructure.messaging.mqtt.translation;

import com.spottrack.platform.monitoring.application.ports.PayloadProfileCatalog;
import com.spottrack.platform.shared.application.result.Result;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.jackson.autoconfigure.JacksonAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CompactSensorPayloadTranslationWiringTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(JacksonAutoConfiguration.class))
            .withUserConfiguration(DefaultSensorPayloadTranslator.class, CompactSensorPayloadTranslator.class,
                    SensorPayloadTranslatorSelector.class);

    @Test
    void catalogSupportsTheCompactEmulatorProfileAndStillTheDefaultOne() {
        contextRunner.run(context -> {
            assertNull(context.getStartupFailure());

            var catalog = context.getBean(PayloadProfileCatalog.class);

            assertTrue(catalog.isSupported("compact-emulator"));
            assertTrue(catalog.isSupported("default"));
            assertTrue(catalog.isSupported(null));
            assertFalse(catalog.isSupported("compact"));
        });
    }

    @Test
    void selectorResolvesTheCompactTranslatorBackedBySpringsJacksonMapper() {
        contextRunner.run(context -> {
            var selection = context.getBean(SensorPayloadTranslatorSelector.class).select("compact-emulator");

            assertInstanceOf(Result.Success.class, selection);
            var translator = selection.toOptional().orElseThrow();
            assertInstanceOf(CompactSensorPayloadTranslator.class, translator);

            var translation = translator.translate("{\"dev\":\"dev-1\",\"sig\":\"mv\",\"dur\":2,\"ts\":\"2026-11-15T12:00:00Z\"}");

            assertInstanceOf(Result.Success.class, translation);
            assertEquals(new TelemetrySignal("dev-1", "MOTION", 2000, "2026-11-15T12:00:00Z"),
                    translation.toOptional().orElseThrow());
        });
    }
}
