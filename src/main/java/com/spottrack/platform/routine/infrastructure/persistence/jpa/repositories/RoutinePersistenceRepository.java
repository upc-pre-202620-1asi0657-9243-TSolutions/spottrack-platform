package com.spottrack.platform.routine.infrastructure.persistence.jpa.repositories;

import com.spottrack.platform.routine.infrastructure.persistence.jpa.entities.RoutinePersistenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoutinePersistenceRepository extends JpaRepository<RoutinePersistenceEntity, Long> {

    @Query("select r from RoutinePersistenceEntity r where r.clientId = :clientId")
    List<RoutinePersistenceEntity> findAllByClientId(@Param("clientId") Long clientId);
}
