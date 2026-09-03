package com.spottrack.platform.monitoring.domain.model.valueobjects;

public record ReportId(Long Id) {
    public ReportId {
        if (Id == null || Id == 0){
            throw new IllegalArgumentException("Id cant be zero or null");
        }

    }

}
