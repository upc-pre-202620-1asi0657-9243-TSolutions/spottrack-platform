package com.spottrack.platform.routine.infrastructure.persistence.jpa.repositories;

import com.spottrack.platform.routine.infrastructure.persistence.jpa.entities.RoutineSessionPersistenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoutineSessionPersistenceRepository extends JpaRepository<RoutineSessionPersistenceEntity, Long> {

    @Query("select rs from RoutineSessionPersistenceEntity rs where rs.clientId = :clientId")
    List<RoutineSessionPersistenceEntity> findAllByClientId(@Param("clientId") Long clientId);
}
