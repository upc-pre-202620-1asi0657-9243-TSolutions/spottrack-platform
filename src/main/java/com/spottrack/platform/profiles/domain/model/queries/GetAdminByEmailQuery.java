package com.spottrack.platform.profiles.domain.model.queries;

import com.spottrack.platform.profiles.domain.model.valueobjects.EmailAddress;

public record GetAdminByEmailQuery(EmailAddress emailAddress) {
    public GetAdminByEmailQuery{
        if (emailAddress == null || emailAddress.getAddress().isBlank()){
            throw new IllegalArgumentException("Email is required");
        }
    }
}
