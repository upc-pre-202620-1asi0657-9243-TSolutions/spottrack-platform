package com.spottrack.platform.routine.infrastructure.persistence.jpa.entities;

import com.spottrack.platform.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "exercise_blocks")
@Getter
@Setter
@NoArgsConstructor
public class ExerciseBlockPersistenceEntity extends AuditableAbstractPersistenceEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "routine_id", nullable = false)
    private RoutinePersistenceEntity routine;

    @Column(name = "exercise_name")
    private String exerciseName;

    @Column(name = "exercise_type")
    private String exerciseType;

    @Column(name = "exercise_order")
    private int order;

    @Column(name = "`sets`")
    private int sets;

    @Column(name = "reps")
    private int reps;
}
