package com.spottrack.platform.analytics.infrastructure.persistence.jpa.adapters;

import com.spottrack.platform.analytics.domain.model.aggregates.UsageSession;
import com.spottrack.platform.analytics.domain.repositories.UsageSessionRepository;
import com.spottrack.platform.analytics.infrastructure.persistence.jpa.assemblers.UsageSessionPersistenceAssembler;
import com.spottrack.platform.analytics.infrastructure.persistence.jpa.repositories.JpaUsageSessionRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Repository
public class UsageSessionRepositoryImpl implements UsageSessionRepository {

    private final JpaUsageSessionRepository jpaUsageSessionRepository;

    public UsageSessionRepositoryImpl(JpaUsageSessionRepository jpaUsageSessionRepository) {
        this.jpaUsageSessionRepository = jpaUsageSessionRepository;
    }

    @Override
    public UsageSession save(UsageSession usageSession) {
        var saved = jpaUsageSessionRepository.save(UsageSessionPersistenceAssembler.toPersistenceFromDomain(usageSession));
        usageSession.setId(saved.getId());
        return usageSession;
    }

    @Override
    public List<UsageSession> saveAll(List<UsageSession> usageSessions) {
        var entities = usageSessions.stream().map(UsageSessionPersistenceAssembler::toPersistenceFromDomain).toList();
        return jpaUsageSessionRepository.saveAll(entities).stream()
                .map(UsageSessionPersistenceAssembler::toDomainFromPersistence)
                .toList();
    }

    @Override
    public List<UsageSession> findOverlapping(Collection<String> equipmentIds, LocalDateTime from, LocalDateTime to) {
        if (equipmentIds.isEmpty()) return List.of();
        return jpaUsageSessionRepository
                .findByEquipmentIdInAndStartedAtLessThanAndEndedAtGreaterThan(equipmentIds, to, from).stream()
                .map(UsageSessionPersistenceAssembler::toDomainFromPersistence)
                .toList();
    }

    @Override
    public boolean existsByEquipmentId(String equipmentId) {
        return jpaUsageSessionRepository.existsByEquipmentId(equipmentId);
    }
}
