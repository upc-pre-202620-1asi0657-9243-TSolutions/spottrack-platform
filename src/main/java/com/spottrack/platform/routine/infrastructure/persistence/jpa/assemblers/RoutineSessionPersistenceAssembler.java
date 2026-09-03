package com.spottrack.platform.routine.infrastructure.persistence.jpa.assemblers;

import com.spottrack.platform.routine.domain.model.aggregates.RoutineSession;
import com.spottrack.platform.routine.domain.model.entities.SessionExerciseCompletion;
import com.spottrack.platform.routine.domain.model.valueobjects.ClientId;
import com.spottrack.platform.routine.domain.model.valueobjects.RoutineSessionStatus;
import com.spottrack.platform.routine.infrastructure.persistence.jpa.entities.RoutineSessionPersistenceEntity;
import com.spottrack.platform.routine.infrastructure.persistence.jpa.entities.SessionExerciseCompletionPersistenceEntity;

import java.util.List;
import java.util.stream.Collectors;

public final class RoutineSessionPersistenceAssembler {

    private RoutineSessionPersistenceAssembler() {}

    public static RoutineSession toDomainFromPersistence(RoutineSessionPersistenceEntity entity) {
        return new RoutineSession(
                entity.getId(),
                entity.getRoutineId(),
                new ClientId(entity.getClientId()),
                RoutineSessionStatus.valueOf(entity.getStatus()),
                entity.getStartedAt(),
                toDomainCompletions(entity.getCompletedExercises())
        );
    }

    public static RoutineSessionPersistenceEntity toPersistenceFromDomain(RoutineSession session) {
        var entity = new RoutineSessionPersistenceEntity();
        entity.setId(session.getId());
        entity.setRoutineId(session.getRoutineId());
        entity.setClientId(session.getClientId().clientId());
        entity.setStatus(session.getStatus().name());
        entity.setStartedAt(session.getStartedAt());
        entity.setCompletedExercises(toPersistenceCompletions(session.getCompletedExerciseBlockIds(), entity));
        return entity;
    }

    private static List<SessionExerciseCompletion> toDomainCompletions(List<SessionExerciseCompletionPersistenceEntity> entities) {
        if (entities == null) return List.of();
        return entities.stream()
                .map(e -> new SessionExerciseCompletion(e.getId(), e.getExerciseBlockId()))
                .collect(Collectors.toList());
    }

    private static List<SessionExerciseCompletionPersistenceEntity> toPersistenceCompletions(
            List<Long> completedExerciseBlockIds, RoutineSessionPersistenceEntity parent) {
        if (completedExerciseBlockIds == null) return List.of();
        return completedExerciseBlockIds.stream()
                .map(exerciseBlockId -> {
                    var completionEntity = new SessionExerciseCompletionPersistenceEntity();
                    completionEntity.setRoutineSession(parent);
                    completionEntity.setExerciseBlockId(exerciseBlockId);
                    return completionEntity;
                })
                .collect(Collectors.toList());
    }
}
