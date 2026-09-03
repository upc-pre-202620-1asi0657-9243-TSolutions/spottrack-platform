package com.spottrack.platform.profiles.infrastructure.persistence.jpa.repositories;

import com.spottrack.platform.profiles.domain.model.valueobjects.EmailAddress;
import com.spottrack.platform.profiles.infrastructure.persistence.jpa.entities.AdminPersistenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AdminPersistenceRepository extends JpaRepository<AdminPersistenceEntity, Long> {

    Optional<AdminPersistenceEntity> findByUserId(Long userId);

    @Query("select a from AdminPersistenceEntity a where a.emailAddress = :emailAddress")
    Optional<AdminPersistenceEntity> findByEmailAddress(@Param("emailAddress") EmailAddress emailAddress);

    @Query("select count(a) from AdminPersistenceEntity a where a.emailAddress = :emailAddress")
    long countByEmailAddress(@Param("emailAddress") EmailAddress emailAddress);
}
