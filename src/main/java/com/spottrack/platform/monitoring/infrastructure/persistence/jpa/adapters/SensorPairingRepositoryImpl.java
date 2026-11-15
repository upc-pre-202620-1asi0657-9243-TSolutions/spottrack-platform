package com.spottrack.platform.monitoring.infrastructure.persistence.jpa.adapters;

import com.spottrack.platform.monitoring.domain.model.aggregates.SensorPairing;
import com.spottrack.platform.monitoring.domain.model.valueobjects.MotionSensorId;
import com.spottrack.platform.monitoring.domain.model.valueobjects.SensorPairingStatus;
import com.spottrack.platform.monitoring.domain.repositories.SensorPairingRepository;
import com.spottrack.platform.monitoring.infrastructure.persistence.jpa.assemblers.SensorPairingPersistenceAssembler;
import com.spottrack.platform.monitoring.infrastructure.persistence.jpa.repositories.SensorPairingPersistenceRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Repository
public class SensorPairingRepositoryImpl implements SensorPairingRepository {
    private final SensorPairingPersistenceRepository sensorPairingPersistenceRepository;
    private final ApplicationEventPublisher eventPublisher;

    public SensorPairingRepositoryImpl(SensorPairingPersistenceRepository sensorPairingPersistenceRepository,
                                       ApplicationEventPublisher eventPublisher) {
        this.sensorPairingPersistenceRepository = sensorPairingPersistenceRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public SensorPairing save(SensorPairing sensorPairing) {
        var savedEntity = sensorPairingPersistenceRepository.save(
                SensorPairingPersistenceAssembler.toPersistenceFromDomain(sensorPairing));
        sensorPairing.domainEvents().forEach(eventPublisher::publishEvent);
        sensorPairing.clearDomainEvents();
        return SensorPairingPersistenceAssembler.toDomainFromPersistence(savedEntity);
    }

    @Override
    public List<SensorPairing> findPendingByMotionSensorId(MotionSensorId motionSensorId) {
        return findByStatuses(motionSensorId, Set.of(SensorPairingStatus.PENDING));
    }

    @Override
    public List<SensorPairing> findActiveByMotionSensorId(MotionSensorId motionSensorId) {
        return findByStatuses(motionSensorId, Set.of(SensorPairingStatus.PENDING, SensorPairingStatus.USED));
    }

    @Override
    public List<SensorPairing> findPendingExpiredBefore(LocalDateTime now) {
        return sensorPairingPersistenceRepository.findByStatusAndExpiresAtBefore(SensorPairingStatus.PENDING, now).stream()
                .map(SensorPairingPersistenceAssembler::toDomainFromPersistence)
                .toList();
    }

    private List<SensorPairing> findByStatuses(MotionSensorId motionSensorId, Set<SensorPairingStatus> statuses) {
        return sensorPairingPersistenceRepository.findByMotionSensorIdAndStatusIn(motionSensorId, statuses).stream()
                .map(SensorPairingPersistenceAssembler::toDomainFromPersistence)
                .toList();
    }
}
