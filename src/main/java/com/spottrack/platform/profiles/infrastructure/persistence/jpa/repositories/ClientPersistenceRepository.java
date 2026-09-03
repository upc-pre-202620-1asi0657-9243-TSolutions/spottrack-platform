package com.spottrack.platform.profiles.infrastructure.persistence.jpa.repositories;

import com.spottrack.platform.profiles.domain.model.valueobjects.EmailAddress;
import com.spottrack.platform.profiles.infrastructure.persistence.jpa.entities.ClientPersistenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClientPersistenceRepository extends JpaRepository<ClientPersistenceEntity, Long> {

    Optional<ClientPersistenceEntity> findByUserId(Long userId);

    @Query("select c from ClientPersistenceEntity c where c.emailAddress = :emailAddress")
    Optional<ClientPersistenceEntity> findByEmailAddress(@Param("emailAddress") EmailAddress emailAddress);

    @Query("select count(c) from ClientPersistenceEntity c where c.emailAddress = :emailAddress")
    long countByEmailAddress(@Param("emailAddress") EmailAddress emailAddress);
}
