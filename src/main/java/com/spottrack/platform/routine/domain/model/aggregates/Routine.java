package com.spottrack.platform.routine.domain.model.aggregates;
import com.spottrack.platform.routine.domain.model.valueobjects.ClientId;
import com.spottrack.platform.routine.domain.model.commands.CreateRoutineCommand;
import com.spottrack.platform.routine.domain.model.entities.ExerciseBlock;
import com.spottrack.platform.routine.domain.model.events.ExerciseBlockAddedEvent;
import com.spottrack.platform.routine.domain.model.events.RoutineCreatedEvent;
import com.spottrack.platform.routine.domain.model.valueobjects.ExerciseName;
import com.spottrack.platform.routine.domain.model.valueobjects.ExerciseType;
import com.spottrack.platform.routine.domain.model.valueobjects.RoutineName;
import com.spottrack.platform.shared.domain.model.aggregates.AbstractDomainAggregateRoot;
import lombok.Getter;
import lombok.Setter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Routine extends AbstractDomainAggregateRoot<Routine> {
    @Getter
    @Setter
    private Long id;
    @Getter
    private RoutineName routineName;
    private ClientId clientId;
    private List<ExerciseBlock> exerciseBlocks;

    public Routine(Long id, RoutineName routineName, ClientId clientId, List<ExerciseBlock> exerciseBlocks) {
        this.id = id;
        this.routineName = Objects.requireNonNull(routineName, "Routine name must not be null");
        this.clientId = Objects.requireNonNull(clientId, "Profile id must not be null");
        this.exerciseBlocks = exerciseBlocks != null ? exerciseBlocks : new ArrayList<>();
    }

    public Routine(CreateRoutineCommand command) {
        this(null, command.routineName(), command.clientId(), new ArrayList<>());
    }

    public void addExerciseBlock(ExerciseName exerciseName, ExerciseType exerciseType, int order, int sets, int reps) {
        var block = new ExerciseBlock(null, exerciseName, exerciseType, order, sets, reps);
        this.exerciseBlocks.add(block);
        registerDomainEvent(ExerciseBlockAddedEvent.from(this, block));
    }

    public void onCreated() {
        registerDomainEvent(RoutineCreatedEvent.from(this));
    }

    public ClientId getClientId() { return clientId; }
    public List<ExerciseBlock> getExerciseBlocks() { return exerciseBlocks; }
}