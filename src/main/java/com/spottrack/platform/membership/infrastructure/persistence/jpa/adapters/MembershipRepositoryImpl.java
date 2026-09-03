package com.spottrack.platform.membership.infrastructure.persistence.jpa.adapters;

import com.spottrack.platform.membership.domain.model.aggregates.Membership;
import com.spottrack.platform.membership.domain.model.valueobjects.MembershipId;
import com.spottrack.platform.membership.domain.model.valueobjects.MembershipStatus;
import com.spottrack.platform.membership.domain.repositories.MembershipRepository;
import com.spottrack.platform.membership.infrastructure.persistence.jpa.assemblers.MembershipPersistenceAssembler;
import com.spottrack.platform.membership.infrastructure.persistence.jpa.repositories.MembershipPersistenceRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public class MembershipRepositoryImpl implements MembershipRepository {

    private final MembershipPersistenceRepository membershipPersistenceRepository;
    private final ApplicationEventPublisher eventPublisher;

    public MembershipRepositoryImpl(
            MembershipPersistenceRepository membershipPersistenceRepository,
            ApplicationEventPublisher eventPublisher) {
        this.membershipPersistenceRepository = membershipPersistenceRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public Optional<Membership> findByMembershipId(MembershipId membershipId) {
        return membershipPersistenceRepository.findByMembershipId(membershipId.uuid().toString())
                .map(MembershipPersistenceAssembler::toDomainFromPersistence);
    }

    @Override
    public Optional<Membership> findById(Long id) {
        return membershipPersistenceRepository.findById(id)
                .map(MembershipPersistenceAssembler::toDomainFromPersistence);
    }

    @Override
    public List<Membership> findByClientId(Long clientId) {
        return membershipPersistenceRepository.findByClientId(clientId).stream()
                .map(MembershipPersistenceAssembler::toDomainFromPersistence)
                .toList();
    }

    @Override
    public Membership save(Membership membership) {
        boolean isNew = membership.getId() == null;
        var savedEntity = membershipPersistenceRepository.save(
                MembershipPersistenceAssembler.toPersistenceFromDomain(membership));
        var savedMembership = MembershipPersistenceAssembler.toDomainFromPersistence(savedEntity);
        if (isNew) {
            savedMembership.onCreated();
            savedMembership.domainEvents().forEach(eventPublisher::publishEvent);
            savedMembership.clearDomainEvents();
        } else {
            membership.domainEvents().forEach(eventPublisher::publishEvent);
            membership.clearDomainEvents();
        }
        return savedMembership;
    }

    @Override
    public boolean existsByMembershipId(MembershipId membershipId) {
        return membershipPersistenceRepository.existsByMembershipId(membershipId.uuid().toString());
    }

    @Override
    public List<Membership> findActiveExpiredBefore(LocalDate date) {
        var expirableStatuses = Set.of(MembershipStatus.ACTIVE, MembershipStatus.SUSPENDED);
        return membershipPersistenceRepository.findByStatusInAndEndDateBefore(expirableStatuses, date)
                .stream()
                .filter(e -> !e.isCancelAtPeriodEnd())
                .map(MembershipPersistenceAssembler::toDomainFromPersistence)
                .toList();
    }

    @Override
    public List<Membership> findPendingCancellationExpiredBefore(LocalDate date) {
        return membershipPersistenceRepository
                .findByStatusAndCancelAtPeriodEndTrueAndEndDateBefore(MembershipStatus.ACTIVE, date)
                .stream()
                .map(MembershipPersistenceAssembler::toDomainFromPersistence)
                .toList();
    }
}
