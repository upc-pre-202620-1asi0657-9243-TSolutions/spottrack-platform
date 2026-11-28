package com.spottrack.platform.profiles.domain.model.valueobjects;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Duration;
import java.util.Random;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

/** The linear email regex must accept exactly what the former backtracking one accepted. */
class EmailAddressRegexTest {

    private static final Pattern LEGACY =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern CURRENT = Pattern.compile(EmailAddress.REGEX);

    @ParameterizedTest
    @ValueSource(strings = {"owner@spotgym.pe", "a.b+c_d-e@mail.co", "x@a..com", "x@a.b.c.org", "x@-.io"})
    void acceptsValidAddresses(String address) {
        assertDoesNotThrow(() -> new EmailAddress(address));
    }

    @ParameterizedTest
    @ValueSource(strings = {"owner", "owner@", "@spotgym.pe", "x@.com", "x@spotgym", "x@spotgym.", "x@spotgym.c",
            "x@spot.gym1", "x@@spot.pe", "x y@spot.pe"})
    void rejectsInvalidAddresses(String address) {
        assertThrows(IllegalArgumentException.class, () -> new EmailAddress(address));
    }

    @Test
    void acceptsTheSameStringsAsTheLegacyRegex() {
        var alphabet = "ab9.-@+_Z..";
        var random = new Random(20261127L);
        for (int i = 0; i < 200_000; i++) {
            var sb = new StringBuilder();
            for (int j = random.nextInt(14); j > 0; j--) sb.append(alphabet.charAt(random.nextInt(alphabet.length())));
            var candidate = sb.toString();
            assertEquals(LEGACY.matcher(candidate).matches(), CURRENT.matcher(candidate).matches(), candidate);
        }
    }

    @Test
    void rejectsAHostileInputQuickly() {
        var hostile = "a@" + "a.".repeat(20_000) + "1";
        assertTimeoutPreemptively(Duration.ofSeconds(1), () -> assertEquals(false, CURRENT.matcher(hostile).matches()));
    }
}
