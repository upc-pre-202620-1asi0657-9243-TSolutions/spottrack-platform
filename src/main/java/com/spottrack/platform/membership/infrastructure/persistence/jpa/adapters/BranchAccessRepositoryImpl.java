package com.spottrack.platform.membership.infrastructure.persistence.jpa.adapters;

import com.spottrack.platform.membership.domain.model.aggregates.BranchAccess;
import com.spottrack.platform.membership.domain.model.valueobjects.BranchAccessId;
import com.spottrack.platform.membership.domain.repositories.BranchAccessRepository;
import com.spottrack.platform.membership.infrastructure.persistence.jpa.assemblers.BranchAccessPersistenceAssembler;
import com.spottrack.platform.membership.infrastructure.persistence.jpa.repositories.BranchAccessPersistenceRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class BranchAccessRepositoryImpl implements BranchAccessRepository {

    private final BranchAccessPersistenceRepository branchAccessPersistenceRepository;
    private final ApplicationEventPublisher eventPublisher;

    public BranchAccessRepositoryImpl(
            BranchAccessPersistenceRepository branchAccessPersistenceRepository,
            ApplicationEventPublisher eventPublisher) {
        this.branchAccessPersistenceRepository = branchAccessPersistenceRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public Optional<BranchAccess> findByBranchAccessId(BranchAccessId branchAccessId) {
        return branchAccessPersistenceRepository.findByBranchAccessId(branchAccessId.uuid().toString())
                .map(BranchAccessPersistenceAssembler::toDomainFromPersistence);
    }

    @Override
    public List<BranchAccess> findByMembershipId(Long membershipId) {
        return branchAccessPersistenceRepository.findByMembershipId(membershipId).stream()
                .map(BranchAccessPersistenceAssembler::toDomainFromPersistence)
                .toList();
    }

    @Override
    public BranchAccess save(BranchAccess branchAccess) {
        boolean isNew = branchAccess.getId() == null;
        var savedEntity = branchAccessPersistenceRepository.save(
                BranchAccessPersistenceAssembler.toPersistenceFromDomain(branchAccess));
        var savedBranchAccess = BranchAccessPersistenceAssembler.toDomainFromPersistence(savedEntity);
        if (isNew) {
            savedBranchAccess.onCreated();
            savedBranchAccess.domainEvents().forEach(eventPublisher::publishEvent);
            savedBranchAccess.clearDomainEvents();
        }
        return savedBranchAccess;
    }
}
