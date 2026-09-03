package com.spottrack.platform.monitoring.application.queryServices;

import com.spottrack.platform.monitoring.domain.model.aggregates.SessionTracker;
import com.spottrack.platform.monitoring.domain.model.queries.GetAllSessionTrackersQuery;
import com.spottrack.platform.monitoring.domain.model.queries.GetSessionTrackerByIdQuery;
import com.spottrack.platform.monitoring.domain.model.queries.GetSessionTrackerByReservationIdQuery;
import com.spottrack.platform.shared.application.result.ApplicationError;

import java.util.List;
import java.util.Optional;

public interface SessionTrackerQueryService {
    Optional<SessionTracker> handle(GetSessionTrackerByIdQuery query);
    Optional<SessionTracker> handle(GetSessionTrackerByReservationIdQuery query);
    List<SessionTracker> handle(GetAllSessionTrackersQuery query);

}
