package com.spottrack.platform.analytics.application.outboundservices;

import com.spottrack.platform.analytics.domain.model.valueobjects.UsageReport;

public interface UsageReportRenderer {
    /** Renders the report as a PDF document. */
    byte[] render(UsageReport report);
}
