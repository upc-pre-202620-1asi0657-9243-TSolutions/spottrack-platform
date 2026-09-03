package com.spottrack.platform.routine.infrastructure.persistence.jpa.embeddables;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class RoutineNamePersistenceEmbeddable {

    @Column(name = "routine_name")
    private String routineName;

    public RoutineNamePersistenceEmbeddable() {
    }

    public RoutineNamePersistenceEmbeddable(String routineName) {
        this.routineName = routineName;
    }

    public String getRoutineName() { return routineName; }
    public void setRoutineName(String routineName) { this.routineName = routineName; }
}
