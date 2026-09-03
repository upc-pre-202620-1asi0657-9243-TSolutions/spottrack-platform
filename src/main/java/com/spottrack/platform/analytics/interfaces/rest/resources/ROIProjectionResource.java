package com.spottrack.platform.analytics.interfaces.rest.resources;

public record ROIProjectionResource(
        Long id,
        Long roiProjectionId,
        Double requestedDowntimeCost,
        Double requestedEarnings,
        Double roiIndex,
        String demandStatus
) {}
