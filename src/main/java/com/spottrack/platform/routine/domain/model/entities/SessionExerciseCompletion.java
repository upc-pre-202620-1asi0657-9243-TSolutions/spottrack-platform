package com.spottrack.platform.routine.domain.model.entities;

import lombok.Getter;

@Getter
public class SessionExerciseCompletion {
    private Long id;
    private Long exerciseBlockId;

    public SessionExerciseCompletion(Long id, Long exerciseBlockId) {
        this.id = id;
        this.exerciseBlockId = exerciseBlockId;
    }
}
