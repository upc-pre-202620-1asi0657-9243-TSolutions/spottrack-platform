package com.spottrack.platform.iam.domain.model.queries;

import com.spottrack.platform.iam.domain.model.valueobjects.Roles;

public record GetRoleByNameQuery(Roles name) {
}
