package com.spottrack.platform.analytics.infrastructure.persistence.jpa.converters;

import com.spottrack.platform.analytics.domain.model.valueobjects.MaintenanceQuoteId;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class MaintenanceQuoteIdPersistenceConverter implements AttributeConverter<MaintenanceQuoteId, Long> {
    @Override
    public Long convertToDatabaseColumn(MaintenanceQuoteId attribute) {
        return attribute == null ? null : attribute.value();
    }

    @Override
    public MaintenanceQuoteId convertToEntityAttribute(Long dbData) {
        return dbData == null ? null : new MaintenanceQuoteId(dbData);
    }
}
