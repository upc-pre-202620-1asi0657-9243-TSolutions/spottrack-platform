package com.spottrack.platform.iam.interfaces.rest.resources;

import jakarta.validation.constraints.Pattern;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** The @Pattern on the technician email must accept exactly what the former backtracking regex accepted. */
class RegisterTechnicianResourceEmailRegexTest {

    private static final java.util.regex.Pattern LEGACY =
            java.util.regex.Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    @Test
    void emailPatternAcceptsTheSameStringsAsTheLegacyRegex() throws NoSuchFieldException {
        var annotation = RegisterTechnicianResource.class.getDeclaredField("email").getAnnotation(Pattern.class);
        var current = java.util.regex.Pattern.compile(annotation.regexp());

        var alphabet = "ab9.-@+_Z..";
        var random = new Random(20261127L);
        for (int i = 0; i < 200_000; i++) {
            var sb = new StringBuilder();
            for (int j = random.nextInt(14); j > 0; j--) sb.append(alphabet.charAt(random.nextInt(alphabet.length())));
            var candidate = sb.toString();
            assertEquals(LEGACY.matcher(candidate).matches(), current.matcher(candidate).matches(), candidate);
        }
    }
}
