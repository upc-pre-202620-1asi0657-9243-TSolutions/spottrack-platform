package com.spottrack.platform.iam.domain.repositories;

import com.spottrack.platform.iam.domain.model.entities.Role;
import com.spottrack.platform.iam.domain.model.valueobjects.Roles;

import java.util.List;
import java.util.Optional;

public interface RoleRepository {
    Optional<Role> findByName(Roles name);
    boolean existsByName(Roles name);
    Role save(Role role);
    List<Role> findAll();
}
