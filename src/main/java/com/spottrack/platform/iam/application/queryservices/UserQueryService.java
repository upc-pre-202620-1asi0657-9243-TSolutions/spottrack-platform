package com.spottrack.platform.iam.application.queryservices;

import com.spottrack.platform.iam.domain.model.aggregates.User;
import com.spottrack.platform.iam.domain.model.queries.GetAllUsersQuery;
import com.spottrack.platform.iam.domain.model.queries.GetUserByIdQuery;
import com.spottrack.platform.iam.domain.model.queries.GetUserByUsernameQuery;

import java.util.List;
import java.util.Optional;

public interface UserQueryService {
    List<User> handle(GetAllUsersQuery query);
    Optional<User> handle(GetUserByIdQuery query);
    Optional<User> handle(GetUserByUsernameQuery query);
}
