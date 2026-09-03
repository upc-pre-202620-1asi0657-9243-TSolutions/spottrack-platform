package com.spottrack.platform.routine.infrastructure.persistence.jpa.entities;

import com.spottrack.platform.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "session_exercise_completions")
public class SessionExerciseCompletionPersistenceEntity extends AuditableAbstractPersistenceEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "routine_session_id", nullable = false)
    private RoutineSessionPersistenceEntity routineSession;

    @Column(name = "exercise_block_id", nullable = false)
    private Long exerciseBlockId;

    public RoutineSessionPersistenceEntity getRoutineSession() { return routineSession; }
    public void setRoutineSession(RoutineSessionPersistenceEntity routineSession) { this.routineSession = routineSession; }

    public Long getExerciseBlockId() { return exerciseBlockId; }
    public void setExerciseBlockId(Long exerciseBlockId) { this.exerciseBlockId = exerciseBlockId; }
}
