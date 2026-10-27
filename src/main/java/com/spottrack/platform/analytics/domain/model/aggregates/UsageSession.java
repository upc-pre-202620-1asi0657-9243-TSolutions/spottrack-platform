package com.spottrack.platform.analytics.domain.model.aggregates;

import com.spottrack.platform.analytics.domain.model.valueobjects.UsageSessionId;
import com.spottrack.platform.shared.domain.model.aggregates.AbstractDomainAggregateRoot;
import lombok.Getter;
import lombok.Setter;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * One finished period of real equipment use, as measured by its sensors.
 * ActivityReport only keeps a running total per equipment; this keeps each
 * session on the timeline so usage can be grouped by hour, day or zone.
 */
@Getter
public class UsageSession extends AbstractDomainAggregateRoot<UsageSession> {

    @Setter
    private Long id;

    private UsageSessionId usageSessionId;
    private String sessionTrackerId;
    private String equipmentId;
    private LocalDateTime startedAt;
    private LocalDateTime endedAt;

    public UsageSession(String sessionTrackerId, String equipmentId, LocalDateTime startedAt, LocalDateTime endedAt) {
        if (equipmentId == null || equipmentId.isBlank()) {
            throw new IllegalArgumentException("equipmentId must not be blank");
        }
        if (startedAt == null || endedAt == null || endedAt.isBefore(startedAt)) {
            throw new IllegalArgumentException("A usage session needs a start before its end");
        }
        this.usageSessionId = new UsageSessionId(UUID.randomUUID().toString());
        this.sessionTrackerId = sessionTrackerId;
        this.equipmentId = equipmentId;
        this.startedAt = startedAt;
        this.endedAt = endedAt;
    }

    public UsageSession(Long id, UsageSessionId usageSessionId, String sessionTrackerId, String equipmentId,
                        LocalDateTime startedAt, LocalDateTime endedAt) {
        this.id = id;
        this.usageSessionId = usageSessionId;
        this.sessionTrackerId = sessionTrackerId;
        this.equipmentId = equipmentId;
        this.startedAt = startedAt;
        this.endedAt = endedAt;
    }

    /** Minutes of this session that fall inside [from, to); zero when they don't overlap. */
    public double minutesWithin(LocalDateTime from, LocalDateTime to) {
        var start = startedAt.isAfter(from) ? startedAt : from;
        var end = endedAt.isBefore(to) ? endedAt : to;
        return end.isAfter(start) ? Duration.between(start, end).toSeconds() / 60.0 : 0.0;
    }
}
