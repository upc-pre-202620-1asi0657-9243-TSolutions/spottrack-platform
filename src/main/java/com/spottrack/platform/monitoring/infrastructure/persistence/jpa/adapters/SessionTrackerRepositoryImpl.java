package com.spottrack.platform.monitoring.infrastructure.persistence.jpa.adapters;

import com.spottrack.platform.monitoring.domain.model.aggregates.SessionTracker;
import com.spottrack.platform.monitoring.domain.model.valueobjects.EquipmentId;
import com.spottrack.platform.monitoring.domain.model.valueobjects.ReservationId;
import com.spottrack.platform.monitoring.domain.model.valueobjects.SessionTrackerId;
import com.spottrack.platform.monitoring.domain.repositories.SessionTrackerRepository;
import com.spottrack.platform.monitoring.infrastructure.persistence.jpa.assemblers.SessionTrackerPersistenceAssembler;
import com.spottrack.platform.monitoring.infrastructure.persistence.jpa.repositories.SessionTrackerPersistenceRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class SessionTrackerRepositoryImpl implements SessionTrackerRepository {

    private final SessionTrackerPersistenceRepository sessionTrackerPersistenceRepository;
    private final ApplicationEventPublisher eventPublisher;
    public SessionTrackerRepositoryImpl(SessionTrackerPersistenceRepository sessionTrackerPersistenceRepository, ApplicationEventPublisher eventPublisher){
        this.sessionTrackerPersistenceRepository= sessionTrackerPersistenceRepository;
        this.eventPublisher = eventPublisher;

    }
    @Override
    public Optional<SessionTracker> findSessionByUuid(SessionTrackerId uuid) {
        var entity = sessionTrackerPersistenceRepository.findBySessionTrackerId(uuid.uuid());
        var domain = entity.map(SessionTrackerPersistenceAssembler::toDomainFromPersistence);
        return domain;
    }

    @Override
    public Optional<SessionTracker> findByReservationId(ReservationId reservationId) {
        return sessionTrackerPersistenceRepository.findByReservationId(reservationId.uuid())
                .map(SessionTrackerPersistenceAssembler::toDomainFromPersistence);
    }

    @Override
    public Optional<SessionTracker> findActiveByEquipmentId(EquipmentId equipmentId) {
        return sessionTrackerPersistenceRepository.findFirstByEquipmentIdAndSessionIsActiveTrue(equipmentId.uuid())
                .map(SessionTrackerPersistenceAssembler::toDomainFromPersistence);
    }

    @Override
    public List<SessionTracker> findAllBySessionIsActive(SessionTrackerId uuid, boolean active) {
        return sessionTrackerPersistenceRepository.findAllBySessionIsActiveTrue().stream().map(SessionTrackerPersistenceAssembler::toDomainFromPersistence).toList();
    }

    @Override
    public List<SessionTracker> findAll() {
        return sessionTrackerPersistenceRepository.findAll().stream()
                .map(SessionTrackerPersistenceAssembler::toDomainFromPersistence)
                .toList();
    }

    @Override
    public SessionTracker save(SessionTracker sessionTracker) {
        var entity = SessionTrackerPersistenceAssembler.toPersistenceFromDomain(sessionTracker);
        var saved = sessionTrackerPersistenceRepository.save(entity);
        var domain = SessionTrackerPersistenceAssembler.toDomainFromPersistence(saved);
        sessionTracker.domainEvents().forEach(eventPublisher::publishEvent);
        sessionTracker.clearDomainEvents();
        return domain;
    }

    @Override
    public void deleteBySessionTrackerId(SessionTrackerId sessionTrackerId) {
        sessionTrackerPersistenceRepository.deleteBySessionTrackerId(sessionTrackerId.uuid());
    }
}
