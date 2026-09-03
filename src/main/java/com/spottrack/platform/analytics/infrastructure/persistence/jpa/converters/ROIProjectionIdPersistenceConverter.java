package com.spottrack.platform.analytics.infrastructure.persistence.jpa.converters;

import com.spottrack.platform.analytics.domain.model.valueobjects.ROIProjectionId;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class ROIProjectionIdPersistenceConverter implements AttributeConverter<ROIProjectionId, Long> {
    @Override
    public Long convertToDatabaseColumn(ROIProjectionId attribute) {
        return attribute == null ? null : attribute.value();
    }

    @Override
    public ROIProjectionId convertToEntityAttribute(Long dbData) {
        return dbData == null ? null : new ROIProjectionId(dbData);
    }
}
