package com.spottrack.platform.maintenance.interfaces.acl;

import com.spottrack.platform.shared.application.result.ApplicationError;
import com.spottrack.platform.shared.application.result.Result;

public interface MaintenanceContextFacade {

    /**
     * Registers a technician in the maintenance roster linked to an IAM user and the admin that owns it.
     * Returns the technicianId used in tickets and assignments.
     */
    Result<String, ApplicationError> provisionTechnician(Long userId, Long adminUserId, String name);
}
