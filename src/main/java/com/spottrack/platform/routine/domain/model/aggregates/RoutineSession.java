package com.spottrack.platform.routine.domain.model.aggregates;

import com.spottrack.platform.routine.domain.model.commands.StartRoutineCommand;
import com.spottrack.platform.routine.domain.model.entities.SessionExerciseCompletion;
import com.spottrack.platform.routine.domain.model.events.RoutineCompletedEvent;
import com.spottrack.platform.routine.domain.model.events.RoutineMissedEvent;
import com.spottrack.platform.routine.domain.model.events.RoutineStartedEvent;
import com.spottrack.platform.routine.domain.model.valueobjects.ClientId;
import com.spottrack.platform.routine.domain.model.valueobjects.RoutineSessionStatus;
import com.spottrack.platform.shared.domain.model.aggregates.AbstractDomainAggregateRoot;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class RoutineSession extends AbstractDomainAggregateRoot<RoutineSession> {

    @Getter
    @Setter
    private Long id;

    @Getter
    private Long routineId;
    private ClientId clientId;

    @Getter
    private RoutineSessionStatus status;

    @Getter
    private Date startedAt;

    private List<SessionExerciseCompletion> completedExercises;

    public RoutineSession(Long id, Long routineId, ClientId clientId, RoutineSessionStatus status, Date startedAt,
                           List<SessionExerciseCompletion> completedExercises) {
        this.id = id;
        this.routineId = Objects.requireNonNull(routineId, "Routine id must not be null");
        this.clientId = Objects.requireNonNull(clientId, "Client id must not be null");
        this.status = Objects.requireNonNull(status, "Status must not be null");
        this.startedAt = Objects.requireNonNull(startedAt, "Started at must not be null");
        this.completedExercises = completedExercises != null ? completedExercises : new ArrayList<>();
    }

    public RoutineSession(StartRoutineCommand command) {
        this(null, command.routineId(), command.clientId(), RoutineSessionStatus.STARTED, new Date(), new ArrayList<>());
    }

    public void complete() {
        if (this.status != RoutineSessionStatus.STARTED) {
            throw new IllegalStateException("routine.error.session.notStarted");
        }
        this.status = RoutineSessionStatus.COMPLETED;
        registerDomainEvent(RoutineCompletedEvent.from(this));
    }

    public void markMissed() {
        if (this.status != RoutineSessionStatus.STARTED) {
            throw new IllegalStateException("routine.error.session.notStarted");
        }
        this.status = RoutineSessionStatus.MISSED;
        registerDomainEvent(RoutineMissedEvent.from(this));
    }

    public void setExerciseCompletion(Long exerciseBlockId, boolean completed) {
        if (this.status != RoutineSessionStatus.STARTED) {
            throw new IllegalStateException("routine.error.session.notStarted");
        }
        boolean alreadyCompleted = completedExercises.stream()
                .anyMatch(c -> c.getExerciseBlockId().equals(exerciseBlockId));
        if (completed && !alreadyCompleted) {
            completedExercises.add(new SessionExerciseCompletion(null, exerciseBlockId));
        } else if (!completed && alreadyCompleted) {
            completedExercises.removeIf(c -> c.getExerciseBlockId().equals(exerciseBlockId));
        }
    }

    public List<Long> getCompletedExerciseBlockIds() {
        return completedExercises.stream().map(SessionExerciseCompletion::getExerciseBlockId).toList();
    }

    public void onCreated() {
        registerDomainEvent(RoutineStartedEvent.from(this));
    }

    public ClientId getClientId() { return clientId; }
}
