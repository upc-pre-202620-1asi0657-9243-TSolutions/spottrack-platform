package com.spottrack.platform.reservation.infrastructure.persistence.jpa.adapters;

import com.spottrack.platform.reservation.domain.model.aggregates.ReservationRequest;
import com.spottrack.platform.reservation.domain.repositories.ReservationRequestRepository;
import com.spottrack.platform.reservation.infrastructure.persistence.jpa.repositories.ReservationRequestPersistenceRepository;
import com.spottrack.platform.reservation.infrastructure.persistence.jpa.assemblers.ReservationRequestPersistenceAssembler;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class ReservationRequestRepositoryImpl implements ReservationRequestRepository {

    private final ReservationRequestPersistenceRepository jpaRepository;
    private final ApplicationEventPublisher eventPublisher;

    public ReservationRequestRepositoryImpl(
            ReservationRequestPersistenceRepository jpaRepository,
            ApplicationEventPublisher eventPublisher) {
        this.jpaRepository = jpaRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public Optional<ReservationRequest> findById(Long id) {
        return jpaRepository.findById(id)
                .map(ReservationRequestPersistenceAssembler::toDomainFromPersistence);
    }

    @Override
    public Optional<ReservationRequest> findByUuid(String uuid) {
        return jpaRepository.findByUuid(uuid)
                .map(ReservationRequestPersistenceAssembler::toDomainFromPersistence);
    }

    @Override
    public ReservationRequest save(ReservationRequest request) {
        boolean isNew = jpaRepository.findByUuid(request.getId().uuid()).isEmpty();
        var savedEntity = jpaRepository.save(ReservationRequestPersistenceAssembler.toPersistenceFromDomain(request));
        var savedRequest = ReservationRequestPersistenceAssembler.toDomainFromPersistence(savedEntity);
        if (isNew) {
            savedRequest.onSubmitted();
            savedRequest.domainEvents().forEach(eventPublisher::publishEvent);
            savedRequest.clearDomainEvents();
        } else {
            request.domainEvents().forEach(eventPublisher::publishEvent);
            request.clearDomainEvents();
        }
        return savedRequest;
    }
}
