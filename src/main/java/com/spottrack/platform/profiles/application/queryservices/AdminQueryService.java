package com.spottrack.platform.profiles.application.queryservices;

import com.spottrack.platform.profiles.domain.model.aggregates.Admin;
import com.spottrack.platform.profiles.domain.model.queries.GetAdminByEmailQuery;
import com.spottrack.platform.profiles.domain.model.queries.GetAdminByIdQuery;
import com.spottrack.platform.profiles.domain.model.queries.GetAdminByUserIdQuery;

import java.util.Optional;

public interface AdminQueryService {
    Optional<Admin> handle(GetAdminByIdQuery query);
    Optional<Admin> handle(GetAdminByEmailQuery query);
    Optional<Admin> handle(GetAdminByUserIdQuery query);
}
