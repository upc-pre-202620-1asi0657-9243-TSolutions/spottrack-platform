package com.spottrack.platform.routine.infrastructure.persistence.jpa.adapters;

import com.spottrack.platform.routine.domain.model.aggregates.Routine;
import com.spottrack.platform.routine.domain.model.valueobjects.ClientId;
import com.spottrack.platform.routine.domain.repositories.RoutineRepository;
import com.spottrack.platform.routine.infrastructure.persistence.jpa.assemblers.RoutinePersistenceAssembler;
import com.spottrack.platform.routine.infrastructure.persistence.jpa.repositories.RoutinePersistenceRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class RoutineRepositoryImpl implements RoutineRepository {

    private final RoutinePersistenceRepository routinePersistenceRepository;
    private final ApplicationEventPublisher eventPublisher;

    public RoutineRepositoryImpl(
            RoutinePersistenceRepository routinePersistenceRepository,
            ApplicationEventPublisher eventPublisher) {
        this.routinePersistenceRepository = routinePersistenceRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Routine> findById(Long id) {
        return routinePersistenceRepository.findById(id)
                .map(RoutinePersistenceAssembler::toDomainFromPersistence);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Routine> findAllByClientId(ClientId clientId) {
        return routinePersistenceRepository.findAllByClientId(clientId.clientId())
                .stream()
                .map(RoutinePersistenceAssembler::toDomainFromPersistence)
                .collect(Collectors.toList());
    }

    @Override
    public Routine save(Routine routine) {
        boolean isNew = routine.getId() == null;
        var savedEntity = routinePersistenceRepository.save(RoutinePersistenceAssembler.toPersistenceFromDomain(routine));
        var savedRoutine = RoutinePersistenceAssembler.toDomainFromPersistence(savedEntity);
        if (isNew) {
            savedRoutine.onCreated();
            savedRoutine.domainEvents().forEach(eventPublisher::publishEvent);
            savedRoutine.clearDomainEvents();
        }
        return savedRoutine;
    }

    @Override
    public boolean existsById(Long id) {
        return routinePersistenceRepository.existsById(id);
    }
}
