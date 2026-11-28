package com.spottrack.platform.analytics.interfaces.rest;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

/** trimDashes replaced the "(?:^-+)|(?:-+$)" and "-+$" regexes of the report file name. */
class UsageAnalyticsControllerTrimDashesTest {

    @ParameterizedTest
    @CsvSource(value = {
            "'',''",
            "'-',''",
            "'---',''",
            "'-gym-',gym",
            "'--gym-central--',gym-central",
            "'gym',gym",
            "'a-b',a-b",
            "'-a--b-',a--b"
    })
    void stripsOnlyTheDashesAtBothEnds(String input, String expected) {
        assertEquals(expected, UsageAnalyticsController.trimDashes(input));
    }

    @Test
    void handlesALongRunOfDashesQuickly() {
        var dashes = "-".repeat(200_000) + "x";
        assertTimeoutPreemptively(Duration.ofSeconds(1),
                () -> assertEquals("x", UsageAnalyticsController.trimDashes(dashes)));
    }
}
