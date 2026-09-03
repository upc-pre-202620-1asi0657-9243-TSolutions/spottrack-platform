package com.spottrack.platform.iam.domain.repositories;

import com.spottrack.platform.iam.domain.model.aggregates.User;

import java.util.List;
import java.util.Optional;

public interface UserRepository {
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);
    User save(User user);
    Optional<User> findById(Long id);
    List<User> findAll();
}
