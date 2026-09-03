package com.spottrack.platform.routine.infrastructure.persistence.jpa.adapters;

import com.spottrack.platform.routine.domain.model.aggregates.RoutineSession;
import com.spottrack.platform.routine.domain.model.valueobjects.ClientId;
import com.spottrack.platform.routine.domain.repositories.RoutineSessionRepository;
import com.spottrack.platform.routine.infrastructure.persistence.jpa.assemblers.RoutineSessionPersistenceAssembler;
import com.spottrack.platform.routine.infrastructure.persistence.jpa.repositories.RoutineSessionPersistenceRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class RoutineSessionRepositoryImpl implements RoutineSessionRepository {

    private final RoutineSessionPersistenceRepository routineSessionPersistenceRepository;
    private final ApplicationEventPublisher eventPublisher;

    public RoutineSessionRepositoryImpl(
            RoutineSessionPersistenceRepository routineSessionPersistenceRepository,
            ApplicationEventPublisher eventPublisher) {
        this.routineSessionPersistenceRepository = routineSessionPersistenceRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<RoutineSession> findById(Long id) {
        return routineSessionPersistenceRepository.findById(id)
                .map(RoutineSessionPersistenceAssembler::toDomainFromPersistence);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoutineSession> findAllByClientId(ClientId clientId) {
        return routineSessionPersistenceRepository.findAllByClientId(clientId.clientId())
                .stream()
                .map(RoutineSessionPersistenceAssembler::toDomainFromPersistence)
                .collect(Collectors.toList());
    }

    @Override
    public RoutineSession save(RoutineSession session) {
        boolean isNew = session.getId() == null;
        var savedEntity = routineSessionPersistenceRepository.save(
                RoutineSessionPersistenceAssembler.toPersistenceFromDomain(session));
        var savedSession = RoutineSessionPersistenceAssembler.toDomainFromPersistence(savedEntity);
        if (isNew) {
            savedSession.onCreated();
            savedSession.domainEvents().forEach(eventPublisher::publishEvent);
            savedSession.clearDomainEvents();
        }
        return savedSession;
    }
}
