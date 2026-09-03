package com.spottrack.platform.monitoring.domain.model.queries;

import com.spottrack.platform.monitoring.domain.model.valueobjects.ReservationId;

public record GetSessionTrackerByReservationIdQuery(ReservationId reservationId) {
}
