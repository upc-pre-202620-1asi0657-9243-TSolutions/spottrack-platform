package com.spottrack.platform.profiles.domain.model.queries;

import com.spottrack.platform.profiles.domain.model.valueobjects.AdminId;

public record GetAdminByIdQuery (AdminId adminId){
    public GetAdminByIdQuery{
        if (adminId == null || adminId.adminId() <= 0){
            throw new IllegalArgumentException("Admin Id cannot be null or less than or equal to 0");
        }
    }
}
