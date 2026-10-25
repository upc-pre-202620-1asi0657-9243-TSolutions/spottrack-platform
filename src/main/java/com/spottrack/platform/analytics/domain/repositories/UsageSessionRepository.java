package com.spottrack.platform.analytics.domain.repositories;

import com.spottrack.platform.analytics.domain.model.aggregates.UsageSession;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface UsageSessionRepository {
    UsageSession save(UsageSession usageSession);
    List<UsageSession> saveAll(List<UsageSession> usageSessions);
    /** Sessions of the given equipment that overlap [from, to) at all. */
    List<UsageSession> findOverlapping(Collection<String> equipmentIds, LocalDateTime from, LocalDateTime to);
    boolean existsByEquipmentId(String equipmentId);
}
