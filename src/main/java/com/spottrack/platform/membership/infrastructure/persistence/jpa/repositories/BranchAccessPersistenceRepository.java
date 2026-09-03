package com.spottrack.platform.membership.infrastructure.persistence.jpa.repositories;

import com.spottrack.platform.membership.infrastructure.persistence.jpa.entities.BranchAccessPersistenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BranchAccessPersistenceRepository extends JpaRepository<BranchAccessPersistenceEntity, Long> {
    Optional<BranchAccessPersistenceEntity> findByBranchAccessId(String branchAccessId);
    List<BranchAccessPersistenceEntity> findByMembershipId(Long membershipId);
}
