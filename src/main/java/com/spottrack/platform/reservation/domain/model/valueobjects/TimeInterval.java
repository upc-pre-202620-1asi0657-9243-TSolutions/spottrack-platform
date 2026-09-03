package com.spottrack.platform.reservation.domain.model.valueobjects;

import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Time;

@Embeddable

public record TimeInterval(Time startTime, Time endTime) {
    private static final Time MIN_TIME = Time.valueOf("00:00:00");
    private static final Time MAX_TIME = Time.valueOf("23:59:00");

    public TimeInterval {
        if (startTime == null || endTime == null) {
            throw new IllegalArgumentException("startTime and endTime must not be null");
        }

        if (!startTime.before(endTime)){
            throw new IllegalArgumentException("startTime must be before endTime");
        }

        if (startTime.before(MIN_TIME) || startTime.after(MAX_TIME))
            throw new IllegalArgumentException("Start time must be between 00:00:00 and 23:59:00");

        if (endTime.before(MIN_TIME) || endTime.after(MAX_TIME)) {
            throw new IllegalArgumentException("End time must be between 00:00:00 and 23:59:00");
        }
    }
}
