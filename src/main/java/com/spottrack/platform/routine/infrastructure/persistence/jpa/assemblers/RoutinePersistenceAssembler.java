package com.spottrack.platform.routine.infrastructure.persistence.jpa.assemblers;

import com.spottrack.platform.routine.domain.model.aggregates.Routine;
import com.spottrack.platform.routine.domain.model.entities.ExerciseBlock;
import com.spottrack.platform.routine.domain.model.valueobjects.ClientId;
import com.spottrack.platform.routine.domain.model.valueobjects.ExerciseName;
import com.spottrack.platform.routine.domain.model.valueobjects.ExerciseType;
import com.spottrack.platform.routine.domain.model.valueobjects.RoutineName;
import com.spottrack.platform.routine.infrastructure.persistence.jpa.embeddables.RoutineNamePersistenceEmbeddable;
import com.spottrack.platform.routine.infrastructure.persistence.jpa.entities.ExerciseBlockPersistenceEntity;
import com.spottrack.platform.routine.infrastructure.persistence.jpa.entities.RoutinePersistenceEntity;

import java.util.List;
import java.util.stream.Collectors;

public final class RoutinePersistenceAssembler {

    private RoutinePersistenceAssembler() {
    }

    public static Routine toDomainFromPersistence(RoutinePersistenceEntity entity) {
        return new Routine(
                entity.getId(),
                new RoutineName(entity.getRoutineName().getRoutineName()),
                new ClientId(entity.getClientId()),
                toDomainExerciseBlocks(entity.getExerciseBlocks()));
    }

    public static RoutinePersistenceEntity toPersistenceFromDomain(Routine routine) {
        var entity = new RoutinePersistenceEntity();
        entity.setId(routine.getId());
        entity.setClientId(routine.getClientId().clientId());
        entity.setRoutineName(new RoutineNamePersistenceEmbeddable(routine.getRoutineName().routineName()));
        entity.setExerciseBlocks(toPersistenceExerciseBlocks(routine.getExerciseBlocks(), entity));
        return entity;
    }

    private static List<ExerciseBlock> toDomainExerciseBlocks(List<ExerciseBlockPersistenceEntity> entities) {
        if (entities == null) return List.of();
        return entities.stream()
                .map(e -> new ExerciseBlock(
                        e.getId(),
                        new ExerciseName(e.getExerciseName()),
                        ExerciseType.valueOf(e.getExerciseType()),
                        e.getOrder(),
                        e.getSets(),
                        e.getReps()))
                .collect(Collectors.toList());
    }

    private static List<ExerciseBlockPersistenceEntity> toPersistenceExerciseBlocks(List<ExerciseBlock> blocks, RoutinePersistenceEntity parent) {
        if (blocks == null) return List.of();
        return blocks.stream()
                .map(b -> {
                    var blockEntity = new ExerciseBlockPersistenceEntity();
                    blockEntity.setId(b.getId());
                    blockEntity.setRoutine(parent);
                    blockEntity.setExerciseName(b.getExerciseName().exerciseName());
                    blockEntity.setExerciseType(b.getExerciseType().name());
                    blockEntity.setOrder(b.getOrder());
                    blockEntity.setSets(b.getSets());
                    blockEntity.setReps(b.getReps());
                    return blockEntity;
                })
                .collect(Collectors.toList());
    }
}
