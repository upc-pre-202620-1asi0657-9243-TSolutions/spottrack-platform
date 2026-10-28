package com.spottrack.platform.routine.application.internal.commandservices;

import com.spottrack.platform.routine.domain.model.aggregates.Routine;
import com.spottrack.platform.routine.domain.model.aggregates.RoutineSession;
import com.spottrack.platform.routine.domain.model.commands.CompleteRoutineCommand;
import com.spottrack.platform.routine.domain.model.commands.MarkRoutineMissedCommand;
import com.spottrack.platform.routine.domain.model.commands.SetExerciseBlockCompletionCommand;
import com.spottrack.platform.routine.domain.model.commands.StartRoutineCommand;
import com.spottrack.platform.routine.domain.model.entities.ExerciseBlock;
import com.spottrack.platform.routine.domain.model.valueobjects.ClientId;
import com.spottrack.platform.routine.domain.model.valueobjects.ExerciseName;
import com.spottrack.platform.routine.domain.model.valueobjects.ExerciseType;
import com.spottrack.platform.routine.domain.model.valueobjects.RoutineName;
import com.spottrack.platform.routine.domain.model.valueobjects.RoutineSessionStatus;
import com.spottrack.platform.routine.domain.repositories.RoutineRepository;
import com.spottrack.platform.routine.domain.repositories.RoutineSessionRepository;
import com.spottrack.platform.shared.application.result.ApplicationError;
import com.spottrack.platform.shared.application.result.Result;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RoutineSessionCommandServiceImplTest {

    private static final Long ROUTINE_ID = 10L;
    private static final Long SESSION_ID = 20L;
    private static final Long BLOCK_ID = 100L;
    private static final Long OTHER_BLOCK_ID = 101L;
    private static final ClientId OWNER = new ClientId(5L);
    private static final ClientId STRANGER = new ClientId(6L);

    private RoutineSessionRepository sessionRepository;
    private RoutineRepository routineRepository;
    private RoutineSessionCommandServiceImpl service;

    @BeforeEach
    void setUp() {
        sessionRepository = mock(RoutineSessionRepository.class);
        routineRepository = mock(RoutineRepository.class);
        service = new RoutineSessionCommandServiceImpl(sessionRepository, routineRepository);
        when(sessionRepository.save(any(RoutineSession.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    private static Routine routineOf(ClientId owner) {
        var blocks = new ArrayList<>(List.of(
                new ExerciseBlock(BLOCK_ID, new ExerciseName("Squats"), ExerciseType.STRENGTH, 1, 3, 12),
                new ExerciseBlock(OTHER_BLOCK_ID, new ExerciseName("Treadmill"), ExerciseType.CARDIO, 2, 1, 10)));
        return new Routine(ROUTINE_ID, new RoutineName("Routine"), owner, blocks);
    }

    private static RoutineSession sessionOf(ClientId owner, RoutineSessionStatus status, Long... completedBlockIds) {
        var session = new RoutineSession(SESSION_ID, ROUTINE_ID, owner, status, new Date(), new ArrayList<>());
        if (status == RoutineSessionStatus.STARTED) {
            for (var blockId : completedBlockIds) session.setExerciseCompletion(blockId, true);
        }
        return session;
    }

    private void givenSession(RoutineSession session) {
        when(sessionRepository.findById(SESSION_ID)).thenReturn(Optional.of(session));
    }

    private void givenRoutine(Routine routine) {
        when(routineRepository.findById(ROUTINE_ID)).thenReturn(Optional.of(routine));
    }

    private static ApplicationError errorOf(Result<RoutineSession, ApplicationError> result) {
        return (ApplicationError) assertInstanceOf(Result.Failure.class, result).error();
    }

    private static RoutineSession valueOf(Result<RoutineSession, ApplicationError> result) {
        return (RoutineSession) assertInstanceOf(Result.Success.class, result).value();
    }

    // ---- start ----

    @Test
    void startCreatesSessionForRoutineOwner() {
        givenRoutine(routineOf(OWNER));
        when(sessionRepository.findActiveByRoutineIdAndClientId(ROUTINE_ID, OWNER)).thenReturn(Optional.empty());

        var session = valueOf(service.handle(new StartRoutineCommand(ROUTINE_ID, OWNER)));

        assertEquals(RoutineSessionStatus.STARTED, session.getStatus());
        assertEquals(OWNER, session.getClientId());
        verify(sessionRepository).save(any(RoutineSession.class));
    }

    @Test
    void startFailsWhenRoutineDoesNotExist() {
        when(routineRepository.findById(ROUTINE_ID)).thenReturn(Optional.empty());

        var error = errorOf(service.handle(new StartRoutineCommand(ROUTINE_ID, OWNER)));

        assertEquals("ROUTINE_NOT_FOUND", error.code());
        verify(sessionRepository, never()).save(any());
    }

    @Test
    void startFailsWhenRoutineBelongsToAnotherClient() {
        givenRoutine(routineOf(OWNER));

        var error = errorOf(service.handle(new StartRoutineCommand(ROUTINE_ID, STRANGER)));

        assertEquals("FORBIDDEN", error.code());
        verify(sessionRepository, never()).save(any());
    }

    @Test
    void startFailsWhenSessionOfSameRoutineIsAlreadyInProgress() {
        givenRoutine(routineOf(OWNER));
        when(sessionRepository.findActiveByRoutineIdAndClientId(ROUTINE_ID, OWNER))
                .thenReturn(Optional.of(sessionOf(OWNER, RoutineSessionStatus.STARTED)));

        var error = errorOf(service.handle(new StartRoutineCommand(ROUTINE_ID, OWNER)));

        assertEquals("ROUTINESESSION_CONFLICT", error.code());
        assertEquals("routine.error.session.alreadyStarted", error.details());
        verify(sessionRepository, never()).save(any());
    }

    // ---- exercise block completion ----

    @Test
    void markingBlockRecordsItInTheSession() {
        givenSession(sessionOf(OWNER, RoutineSessionStatus.STARTED));
        givenRoutine(routineOf(OWNER));

        var session = valueOf(service.handle(
                new SetExerciseBlockCompletionCommand(SESSION_ID, OWNER, BLOCK_ID, true)));

        assertEquals(List.of(BLOCK_ID), session.getCompletedExerciseBlockIds());
        assertEquals(RoutineSessionStatus.STARTED, session.getStatus());
        verify(sessionRepository).save(any(RoutineSession.class));
    }

    @Test
    void markingAnAlreadyCompletedBlockIsIdempotent() {
        givenSession(sessionOf(OWNER, RoutineSessionStatus.STARTED, BLOCK_ID));
        givenRoutine(routineOf(OWNER));

        var session = valueOf(service.handle(
                new SetExerciseBlockCompletionCommand(SESSION_ID, OWNER, BLOCK_ID, true)));

        assertEquals(List.of(BLOCK_ID), session.getCompletedExerciseBlockIds());
    }

    @Test
    void unmarkingABlockRemovesItFromTheSession() {
        givenSession(sessionOf(OWNER, RoutineSessionStatus.STARTED, BLOCK_ID, OTHER_BLOCK_ID));
        givenRoutine(routineOf(OWNER));

        var session = valueOf(service.handle(
                new SetExerciseBlockCompletionCommand(SESSION_ID, OWNER, BLOCK_ID, false)));

        assertEquals(List.of(OTHER_BLOCK_ID), session.getCompletedExerciseBlockIds());
    }

    @Test
    void markingBlockFailsWhenSessionDoesNotExist() {
        when(sessionRepository.findById(SESSION_ID)).thenReturn(Optional.empty());

        var error = errorOf(service.handle(
                new SetExerciseBlockCompletionCommand(SESSION_ID, OWNER, BLOCK_ID, true)));

        assertEquals("ROUTINESESSION_NOT_FOUND", error.code());
    }

    @Test
    void markingBlockFailsWhenSessionBelongsToAnotherClient() {
        givenSession(sessionOf(OWNER, RoutineSessionStatus.STARTED));

        var error = errorOf(service.handle(
                new SetExerciseBlockCompletionCommand(SESSION_ID, STRANGER, BLOCK_ID, true)));

        assertEquals("FORBIDDEN", error.code());
        verify(routineRepository, never()).findById(anyLong());
        verify(sessionRepository, never()).save(any());
    }

    @Test
    void markingBlockFailsWhenBlockDoesNotBelongToTheRoutine() {
        givenSession(sessionOf(OWNER, RoutineSessionStatus.STARTED));
        givenRoutine(routineOf(OWNER));

        var error = errorOf(service.handle(
                new SetExerciseBlockCompletionCommand(SESSION_ID, OWNER, 999L, true)));

        assertEquals("VALIDATION_ERROR", error.code());
        verify(sessionRepository, never()).save(any());
    }

    @Test
    void markingBlockFailsWhenSessionIsNotInProgress() {
        givenSession(sessionOf(OWNER, RoutineSessionStatus.COMPLETED));
        givenRoutine(routineOf(OWNER));

        var error = errorOf(service.handle(
                new SetExerciseBlockCompletionCommand(SESSION_ID, OWNER, BLOCK_ID, true)));

        assertEquals("ROUTINESESSION_CONFLICT", error.code());
        assertEquals("routine.error.session.notStarted", error.details());
        verify(sessionRepository, never()).save(any());
    }

    // ---- complete ----

    @Test
    void completeMarksSessionAsCompleted() {
        givenSession(sessionOf(OWNER, RoutineSessionStatus.STARTED, BLOCK_ID, OTHER_BLOCK_ID));

        var session = valueOf(service.handle(new CompleteRoutineCommand(SESSION_ID, OWNER)));

        assertEquals(RoutineSessionStatus.COMPLETED, session.getStatus());
    }

    @Test
    void completeFailsWhenSessionDoesNotExist() {
        when(sessionRepository.findById(SESSION_ID)).thenReturn(Optional.empty());

        var error = errorOf(service.handle(new CompleteRoutineCommand(SESSION_ID, OWNER)));

        assertEquals("ROUTINESESSION_NOT_FOUND", error.code());
    }

    @Test
    void completeFailsWhenSessionBelongsToAnotherClient() {
        givenSession(sessionOf(OWNER, RoutineSessionStatus.STARTED));

        var error = errorOf(service.handle(new CompleteRoutineCommand(SESSION_ID, STRANGER)));

        assertEquals("FORBIDDEN", error.code());
        verify(sessionRepository, never()).save(any());
    }

    @Test
    void completeFailsWhenSessionIsAlreadyCompleted() {
        givenSession(sessionOf(OWNER, RoutineSessionStatus.COMPLETED));

        var error = errorOf(service.handle(new CompleteRoutineCommand(SESSION_ID, OWNER)));

        assertEquals("ROUTINESESSION_CONFLICT", error.code());
        verify(sessionRepository, never()).save(any());
    }

    // ---- missed ----

    @Test
    void missedMarksSessionAsMissed() {
        givenSession(sessionOf(OWNER, RoutineSessionStatus.STARTED));

        var session = valueOf(service.handle(new MarkRoutineMissedCommand(SESSION_ID, OWNER)));

        assertEquals(RoutineSessionStatus.MISSED, session.getStatus());
    }

    @Test
    void missedFailsWhenSessionBelongsToAnotherClient() {
        givenSession(sessionOf(OWNER, RoutineSessionStatus.STARTED));

        var error = errorOf(service.handle(new MarkRoutineMissedCommand(SESSION_ID, STRANGER)));

        assertEquals("FORBIDDEN", error.code());
        verify(sessionRepository, never()).save(any());
    }

    @Test
    void missedFailsWhenSessionIsNotInProgress() {
        givenSession(sessionOf(OWNER, RoutineSessionStatus.MISSED));

        var error = errorOf(service.handle(new MarkRoutineMissedCommand(SESSION_ID, OWNER)));

        assertEquals("ROUTINESESSION_CONFLICT", error.code());
    }
}
