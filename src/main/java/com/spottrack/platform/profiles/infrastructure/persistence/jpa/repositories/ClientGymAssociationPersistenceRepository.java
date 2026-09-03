package com.spottrack.platform.profiles.infrastructure.persistence.jpa.repositories;

import com.spottrack.platform.profiles.infrastructure.persistence.jpa.entities.ClientGymAssociationPersistenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ClientGymAssociationPersistenceRepository extends JpaRepository<ClientGymAssociationPersistenceEntity, Long> {
    List<ClientGymAssociationPersistenceEntity> findByClientId(Long clientId);
    Optional<ClientGymAssociationPersistenceEntity> findByClientIdAndGymId(Long clientId, String gymId);
    Optional<ClientGymAssociationPersistenceEntity> findByClientIdAndActiveTrue(Long clientId);
    boolean existsByClientIdAndGymId(Long clientId, String gymId);
    List<ClientGymAssociationPersistenceEntity> findAllByClientIdAndActiveTrue(Long clientId);
}
