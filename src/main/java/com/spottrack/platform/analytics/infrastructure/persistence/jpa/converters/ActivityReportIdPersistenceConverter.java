package com.spottrack.platform.analytics.infrastructure.persistence.jpa.converters;

import com.spottrack.platform.analytics.domain.model.valueobjects.ActivityReportId;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class ActivityReportIdPersistenceConverter implements AttributeConverter<ActivityReportId, Long> {
    @Override
    public Long convertToDatabaseColumn(ActivityReportId attribute) {
        return attribute == null ? null : attribute.value();
    }

    @Override
    public ActivityReportId convertToEntityAttribute(Long dbData) {
        return dbData == null ? null : new ActivityReportId(dbData);
    }
}
