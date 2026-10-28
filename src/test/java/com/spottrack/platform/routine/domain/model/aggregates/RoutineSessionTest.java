package com.spottrack.platform.routine.domain.model.aggregates;

import com.spottrack.platform.routine.domain.model.valueobjects.ClientId;
import com.spottrack.platform.routine.domain.model.valueobjects.RoutineSessionStatus;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RoutineSessionTest {

    private static RoutineSession sessionWith(RoutineSessionStatus status) {
        return new RoutineSession(1L, 10L, new ClientId(5L), status, new Date(), new ArrayList<>());
    }

    @Test
    void markingBlockAsCompletedRecordsIt() {
        var session = sessionWith(RoutineSessionStatus.STARTED);

        session.setExerciseCompletion(100L, true);

        assertEquals(List.of(100L), session.getCompletedExerciseBlockIds());
        assertEquals(RoutineSessionStatus.STARTED, session.getStatus());
    }

    @Test
    void markingSameBlockTwiceKeepsASingleCompletion() {
        var session = sessionWith(RoutineSessionStatus.STARTED);

        session.setExerciseCompletion(100L, true);
        session.setExerciseCompletion(100L, true);

        assertEquals(List.of(100L), session.getCompletedExerciseBlockIds());
    }

    @Test
    void unmarkingACompletedBlockRemovesIt() {
        var session = sessionWith(RoutineSessionStatus.STARTED);
        session.setExerciseCompletion(100L, true);
        session.setExerciseCompletion(101L, true);

        session.setExerciseCompletion(100L, false);

        assertEquals(List.of(101L), session.getCompletedExerciseBlockIds());
    }

    @Test
    void unmarkingABlockThatWasNeverCompletedDoesNothing() {
        var session = sessionWith(RoutineSessionStatus.STARTED);

        session.setExerciseCompletion(100L, false);

        assertTrue(session.getCompletedExerciseBlockIds().isEmpty());
    }

    @Test
    void blocksCannotBeMarkedOnACompletedSession() {
        var session = sessionWith(RoutineSessionStatus.COMPLETED);

        var ex = assertThrows(IllegalStateException.class, () -> session.setExerciseCompletion(100L, true));

        assertEquals("routine.error.session.notStarted", ex.getMessage());
    }

    @Test
    void blocksCannotBeMarkedOnAMissedSession() {
        var session = sessionWith(RoutineSessionStatus.MISSED);

        assertThrows(IllegalStateException.class, () -> session.setExerciseCompletion(100L, true));
    }

    @Test
    void completeMovesStartedSessionToCompleted() {
        var session = sessionWith(RoutineSessionStatus.STARTED);

        session.complete();

        assertEquals(RoutineSessionStatus.COMPLETED, session.getStatus());
    }

    @Test
    void completeTwiceIsRejected() {
        var session = sessionWith(RoutineSessionStatus.STARTED);
        session.complete();

        assertThrows(IllegalStateException.class, session::complete);
    }

    @Test
    void markMissedMovesStartedSessionToMissed() {
        var session = sessionWith(RoutineSessionStatus.STARTED);

        session.markMissed();

        assertEquals(RoutineSessionStatus.MISSED, session.getStatus());
    }

    @Test
    void markMissedOnACompletedSessionIsRejected() {
        var session = sessionWith(RoutineSessionStatus.COMPLETED);

        assertThrows(IllegalStateException.class, session::markMissed);
    }
}
