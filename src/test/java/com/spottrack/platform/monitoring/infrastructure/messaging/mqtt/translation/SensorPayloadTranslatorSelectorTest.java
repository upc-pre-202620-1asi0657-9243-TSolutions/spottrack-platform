package com.spottrack.platform.monitoring.infrastructure.messaging.mqtt.translation;

import com.spottrack.platform.shared.application.result.ApplicationError;
import com.spottrack.platform.shared.application.result.Result;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SensorPayloadTranslatorSelectorTest {

    private final SensorPayloadTranslator defaultTranslator = translatorFor("default");
    private final SensorPayloadTranslator vendorTranslator = translatorFor("vendor-x");
    private final SensorPayloadTranslatorSelector selector =
            new SensorPayloadTranslatorSelector(List.of(defaultTranslator, vendorTranslator));

    @Test
    void nullProfileSelectsTheDefaultTranslator() {
        assertSame(defaultTranslator, selected(selector.select(null)));
    }

    @Test
    void blankProfileSelectsTheDefaultTranslator() {
        assertSame(defaultTranslator, selected(selector.select("")));
        assertSame(defaultTranslator, selected(selector.select("   ")));
    }

    @Test
    void knownProfileSelectsItsTranslator() {
        assertSame(vendorTranslator, selected(selector.select("vendor-x")));
    }

    @Test
    void unknownProfileIsAValidationFailure() {
        var result = selector.select("nope");

        assertInstanceOf(Result.Failure.class, result);
        assertEquals("VALIDATION_ERROR",
                ((Result.Failure<SensorPayloadTranslator, ApplicationError>) result).error().code());
    }

    @Test
    void duplicatedProfilesFailAtStartup() {
        var translators = List.of(translatorFor("vendor-x"), translatorFor("vendor-x"));

        assertThrows(IllegalStateException.class, () -> new SensorPayloadTranslatorSelector(translators));
    }

    private static SensorPayloadTranslator selected(Result<SensorPayloadTranslator, ApplicationError> result) {
        assertInstanceOf(Result.Success.class, result);
        return result.toOptional().orElseThrow();
    }

    private static SensorPayloadTranslator translatorFor(String profile) {
        var translator = mock(SensorPayloadTranslator.class);
        when(translator.profile()).thenReturn(profile);
        return translator;
    }
}
