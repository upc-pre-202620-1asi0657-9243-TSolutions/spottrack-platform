package com.spottrack.platform.routine.infrastructure.persistence.jpa.entities;

import com.spottrack.platform.routine.infrastructure.persistence.jpa.embeddables.RoutineNamePersistenceEmbeddable;
import com.spottrack.platform.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "routines")
public class RoutinePersistenceEntity extends AuditableAbstractPersistenceEntity {

    @Column(name = "client_id", nullable = false)
    private Long clientId;

    @Embedded
    private RoutineNamePersistenceEmbeddable routineName;

    @OneToMany(mappedBy = "routine", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ExerciseBlockPersistenceEntity> exerciseBlocks = new ArrayList<>();

    public Long getClientId() { return clientId; }
    public void setClientId(Long clientId) { this.clientId = clientId; }

    public RoutineNamePersistenceEmbeddable getRoutineName() { return routineName; }
    public void setRoutineName(RoutineNamePersistenceEmbeddable routineName) { this.routineName = routineName; }

    public List<ExerciseBlockPersistenceEntity> getExerciseBlocks() { return exerciseBlocks; }
    public void setExerciseBlocks(List<ExerciseBlockPersistenceEntity> exerciseBlocks) { this.exerciseBlocks = exerciseBlocks; }
}
